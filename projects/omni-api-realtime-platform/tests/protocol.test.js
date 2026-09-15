import test from 'node:test';
import assert from 'node:assert/strict';
import http from 'node:http';
import crypto from 'node:crypto';
import WebSocket from 'ws';

import app from '../src/app.js';
import { setupWebSocketServer } from '../src/websocket/wsServer.js';

let server;
let port;
let baseUrl;
let wsUrl;
let wss;

test.before(async () => {
  server = http.createServer(app);
  wss = setupWebSocketServer(server);
  await new Promise((resolve) => {
    server.listen(0, () => {
      port = server.address().port;
      baseUrl = `http://localhost:${port}`;
      wsUrl = `ws://localhost:${port}/ws`;
      resolve();
    });
  });
});

test.after(async () => {
  wss.close();
  await new Promise((resolve) => server.close(resolve));
});

test.describe('1. REST API & Caching / Idempotency', () => {
  let etag = null;

  test('GET /api/v1/products returns 200 with ETag and Cache-Control', async () => {
    const res = await fetch(`${baseUrl}/api/v1/products`);
    assert.equal(res.status, 200);
    assert.ok(res.headers.get('etag'), 'Should have ETag header');
    assert.ok(res.headers.get('cache-control').includes('public, max-age=60'));
    etag = res.headers.get('etag');

    const body = await res.json();
    assert.ok(body.data.length > 0);
  });

  test('GET /api/v1/products with matching If-None-Match returns 304 Not Modified', async () => {
    const res = await fetch(`${baseUrl}/api/v1/products`, {
      headers: { 'If-None-Match': etag },
    });
    assert.equal(res.status, 304);
  });

  test('POST /api/v1/orders with Idempotency-Key guarantees single execution', async () => {
    const idempotencyKey = 'test_idem_key_' + Date.now();
    const orderPayload = {
      customerId: 'cust_integration_test',
      items: [{ productId: 'prod_3', quantity: 1 }],
    };

    // First Call: Cache MISS
    const res1 = await fetch(`${baseUrl}/api/v1/orders`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Idempotency-Key': idempotencyKey,
      },
      body: JSON.stringify(orderPayload),
    });
    assert.equal(res1.status, 201);
    assert.equal(res1.headers.get('x-cache'), 'MISS (Processed)');
    const body1 = await res1.json();
    assert.ok(body1.order.id);

    // Second Call with same Idempotency-Key: Cache HIT
    const res2 = await fetch(`${baseUrl}/api/v1/orders`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Idempotency-Key': idempotencyKey,
      },
      body: JSON.stringify(orderPayload),
    });
    assert.equal(res2.status, 201);
    assert.equal(res2.headers.get('x-cache'), 'HIT (Idempotent replay)');
    const body2 = await res2.json();
    assert.equal(body2.order.id, body1.order.id);
  });
});

test.describe('2. GraphQL Engine', () => {
  test('POST /graphql executes query with no overfetching', async () => {
    const query = `
      query {
        products(limit: 2) {
          id
          name
        }
      }
    `;

    const res = await fetch(`${baseUrl}/graphql`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query }),
    });

    assert.equal(res.status, 200);
    const result = await res.json();
    assert.ok(result.data.products);
    assert.equal(result.data.products.length, 2);
    // Verified no overfetching: price and stock are not present
    assert.equal(result.data.products[0].price, undefined);
    assert.ok(result.data.products[0].name);
  });

  test('POST /graphql executes mutation and updates inventory', async () => {
    const mutation = `
      mutation {
        createOrder(
          customerId: "graphql_tester"
          items: [{ productId: "prod_4", quantity: 1 }]
        ) {
          id
          customerId
          totalAmount
          status
        }
      }
    `;

    const res = await fetch(`${baseUrl}/graphql`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query: mutation }),
    });

    assert.equal(res.status, 200);
    const result = await res.json();
    assert.ok(result.data.createOrder.id);
    assert.equal(result.data.createOrder.customerId, 'graphql_tester');
  });
});

test.describe('3. Server-Sent Events (SSE)', () => {
  test('GET /api/v1/events/stream establishes text/event-stream headers', async () => {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 2500);

    const res = await fetch(`${baseUrl}/api/v1/events/stream`, {
      signal: controller.signal,
    });

    clearTimeout(timeout);
    assert.equal(res.status, 200);
    assert.equal(res.headers.get('content-type'), 'text/event-stream; charset=utf-8');
    assert.equal(res.headers.get('cache-control'), 'no-cache, no-transform');
    assert.equal(res.headers.get('connection'), 'keep-alive');

    // Read the first chunk
    const reader = res.body.getReader();
    const { value } = await reader.read();
    const chunkStr = new TextDecoder().decode(value);
    assert.ok(chunkStr.includes('event: connected'));
    reader.cancel();
  });
});

test.describe('4. HMAC Webhooks & Security Verification', () => {
  const secret = process.env.WEBHOOK_SECRET || 'prod_webhook_shared_secret_secure_98765';

  function signPayload(bodyStr, timestamp) {
    const hmac = crypto.createHmac('sha256', secret);
    hmac.update(`${timestamp}.${bodyStr}`);
    return `sha256=${hmac.digest('hex')}`;
  }

  test('Valid HMAC signature & fresh timestamp returns 200 OK', async () => {
    const payload = JSON.stringify({ event: 'invoice.paid', data: { id: 'inv_123' } });
    const timestamp = Date.now();
    const sig = signPayload(payload, timestamp);

    const res = await fetch(`${baseUrl}/api/v1/webhooks/incoming`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Hub-Signature-256': sig,
        'X-Hub-Timestamp': timestamp.toString(),
      },
      body: payload,
    });

    assert.equal(res.status, 200);
    const body = await res.json();
    assert.equal(body.success, true);
  });

  test('Tampered signature returns 401 Unauthorized', async () => {
    const payload = JSON.stringify({ event: 'invoice.paid', data: { id: 'inv_123' } });
    const timestamp = Date.now();

    const res = await fetch(`${baseUrl}/api/v1/webhooks/incoming`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Hub-Signature-256': 'sha256=invalid_tampered_signature_hex',
        'X-Hub-Timestamp': timestamp.toString(),
      },
      body: payload,
    });

    assert.equal(res.status, 401);
    const body = await res.json();
    assert.equal(body.error, 'Invalid HMAC signature');
  });

  test('Expired timestamp (> 5 min) triggers replay prevention (400 Bad Request)', async () => {
    const payload = JSON.stringify({ event: 'invoice.paid' });
    const expiredTimestamp = Date.now() - 10 * 60 * 1000; // 10 mins ago
    const sig = signPayload(payload, expiredTimestamp);

    const res = await fetch(`${baseUrl}/api/v1/webhooks/incoming`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Hub-Signature-256': sig,
        'X-Hub-Timestamp': expiredTimestamp.toString(),
      },
      body: payload,
    });

    assert.equal(res.status, 400);
    const body = await res.json();
    assert.ok(body.error.includes('Replay attack prevention'));
  });
});

test.describe('5. WebSocket Full-Duplex Transport', () => {
  test('Establishes WS connection, handles PING and SUBSCRIBE', async () => {
    const ws = new WebSocket(wsUrl);

    await new Promise((resolve) => ws.on('open', resolve));
    assert.equal(ws.readyState, WebSocket.OPEN);

    // Test PING / PONG
    const pongPromise = new Promise((resolve) => {
      ws.on('message', (raw) => {
        const msg = JSON.parse(raw.toString());
        if (msg.type === 'PONG') {
          resolve(msg);
        }
      });
    });

    ws.send(JSON.stringify({ type: 'PING', timestamp: Date.now() }));
    const pong = await pongPromise;
    assert.equal(pong.type, 'PONG');

    // Test Topic Subscription
    const subPromise = new Promise((resolve) => {
      ws.on('message', (raw) => {
        const msg = JSON.parse(raw.toString());
        if (msg.type === 'SUBSCRIBED') {
          resolve(msg);
        }
      });
    });

    ws.send(JSON.stringify({ type: 'SUBSCRIBE', topic: 'orders' }));
    const subAck = await subPromise;
    assert.equal(subAck.topic, 'orders');

    ws.close();
  });
});
