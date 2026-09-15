/**
 * Omni-Protocol Realtime Platform - Interactive Client Logic
 */

// --- Global State ---
let wsClient = null;
let sseClient = null;
let sseEventCount = 0;

// --- Tab Switching ---
document.querySelectorAll('.tab-btn').forEach((btn) => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.tab-btn').forEach((b) => b.classList.remove('active'));
    document.querySelectorAll('.panel').forEach((p) => p.classList.remove('active'));

    btn.classList.add('active');
    const target = btn.getAttribute('data-tab');
    document.getElementById(target)?.classList.add('active');
  });
});

// --- Health Probe ---
async function fetchHealth() {
  const consoleEl = document.getElementById('overview-health');
  try {
    const res = await fetch('/health');
    const data = await res.json();
    consoleEl.textContent = JSON.stringify(data, null, 2);
  } catch (err) {
    consoleEl.textContent = 'Error probing health endpoint: ' + err.message;
  }
}
document.getElementById('btn-refresh-health')?.addEventListener('click', fetchHealth);
fetchHealth();

// --- 1. REST API STUDIO ---
const restConsole = document.getElementById('rest-console');
const restBadge = document.getElementById('rest-status-badge');

function genUUID() {
  return 'req_' + Math.random().toString(36).substring(2, 11) + '_' + Date.now();
}

document.getElementById('btn-gen-idempotency')?.addEventListener('click', () => {
  document.getElementById('rest-idempotency-key').value = genUUID();
});

document.getElementById('btn-rest-get-products')?.addEventListener('click', async () => {
  restConsole.textContent = 'Executing GET /api/v1/products ...\n';
  const start = performance.now();
  try {
    const res = await fetch('/api/v1/products');
    const duration = (performance.now() - start).toFixed(1);
    const etag = res.headers.get('ETag');
    const cacheControl = res.headers.get('Cache-Control');
    const data = await res.json();

    restBadge.textContent = `${res.status} ${res.statusText}`;
    restBadge.style.background = 'rgba(59, 130, 246, 0.3)';

    restConsole.textContent = [
      `HTTP/1.1 ${res.status} ${res.statusText} (${duration}ms)`,
      `ETag: ${etag || 'none'}`,
      `Cache-Control: ${cacheControl || 'none'}`,
      `Content-Type: ${res.headers.get('Content-Type')}`,
      '',
      JSON.stringify(data, null, 2),
    ].join('\n');
  } catch (err) {
    restConsole.textContent = `Error: ${err.message}`;
  }
});

document.getElementById('btn-rest-post-order')?.addEventListener('click', async () => {
  const idempotencyKey = document.getElementById('rest-idempotency-key').value.trim();
  const productId = document.getElementById('rest-product-select').value;
  const quantity = parseInt(document.getElementById('rest-qty').value, 10) || 1;

  restConsole.textContent = `Executing POST /api/v1/orders with Idempotency-Key: ${idempotencyKey} ...\n`;
  const start = performance.now();

  try {
    const res = await fetch('/api/v1/orders', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Idempotency-Key': idempotencyKey,
      },
      body: JSON.stringify({
        customerId: 'cust_enterprise_01',
        items: [{ productId, quantity }],
      }),
    });

    const duration = (performance.now() - start).toFixed(1);
    const cacheStatus = res.headers.get('X-Cache') || 'MISS (Processed)';
    const data = await res.json();

    restBadge.textContent = `${res.status} ${res.statusText} [Cache: ${cacheStatus}]`;
    restBadge.style.background = cacheStatus.includes('HIT')
      ? 'rgba(16, 185, 129, 0.3)'
      : 'rgba(59, 130, 246, 0.3)';

    restConsole.textContent = [
      `HTTP/1.1 ${res.status} ${res.statusText} (${duration}ms)`,
      `X-Cache: ${cacheStatus}`,
      `Idempotency-Key: ${idempotencyKey}`,
      `Content-Type: ${res.headers.get('Content-Type')}`,
      '',
      JSON.stringify(data, null, 2),
    ].join('\n');

    fetchHealth();
  } catch (err) {
    restConsole.textContent = `Error: ${err.message}`;
  }
});

// --- 2. GRAPHQL ENGINE ---
const graphqlInput = document.getElementById('graphql-query-input');
const graphqlConsole = document.getElementById('graphql-console');

document.getElementById('btn-graphql-preset-query')?.addEventListener('click', () => {
  graphqlInput.value = `query GetFilteredProducts {
  products(category: "Electronics", limit: 3) {
    id
    name
    price
    stock
  }
}`;
});

document.getElementById('btn-graphql-preset-mutation')?.addEventListener('click', () => {
  graphqlInput.value = `mutation PlaceOrder {
  createOrder(
    customerId: "cust_graphql_user"
    items: [
      { productId: "prod_2", quantity: 1 }
    ]
  ) {
    id
    customerId
    totalAmount
    status
    createdAt
  }
}`;
});

document.getElementById('btn-graphql-run')?.addEventListener('click', async () => {
  const query = graphqlInput.value.trim();
  graphqlConsole.textContent = 'Executing GraphQL Document...\n';
  const start = performance.now();

  try {
    const res = await fetch('/graphql', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query }),
    });

    const duration = (performance.now() - start).toFixed(1);
    const data = await res.json();

    graphqlConsole.textContent = [
      `GraphQL Response (${duration}ms) - Status: ${res.status}`,
      '-------------------------------------------------------',
      JSON.stringify(data, null, 2),
    ].join('\n');

    fetchHealth();
  } catch (err) {
    graphqlConsole.textContent = 'GraphQL Error: ' + err.message;
  }
});

// --- 3. WEBSOCKET PUB/SUB ---
const wsFeed = document.getElementById('ws-event-feed');
const btnWsConnect = document.getElementById('btn-ws-connect');
const btnWsDisconnect = document.getElementById('btn-ws-disconnect');
const btnWsPing = document.getElementById('btn-ws-ping');
const btnWsSub = document.getElementById('btn-ws-sub');
const btnWsUnsub = document.getElementById('btn-ws-unsub');
const btnWsSend = document.getElementById('btn-ws-send');
const wsStatusText = document.getElementById('ws-status-text');

function addWsFeedItem(type, topic, content) {
  const item = document.createElement('div');
  item.className = 'feed-item ws';
  item.innerHTML = `
    <div class="feed-item-header">
      <span style="font-weight:600; color:#34d399;">[${type}] ${topic ? '&bull; ' + topic : ''}</span>
      <span>${new Date().toLocaleTimeString()}</span>
    </div>
    <div class="feed-item-body">${typeof content === 'object' ? JSON.stringify(content) : content}</div>
  `;
  wsFeed.prepend(item);
}

document.getElementById('btn-clear-ws')?.addEventListener('click', () => {
  wsFeed.innerHTML = '';
});

btnWsConnect?.addEventListener('click', () => {
  const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:';
  const url = `${protocol}//${location.host}/ws`;

  addWsFeedItem('CONNECTING', '', `Opening connection to ${url}`);
  wsClient = new WebSocket(url);

  wsClient.onopen = () => {
    wsStatusText.textContent = 'Connected';
    document.getElementById('pill-ws').style.borderColor = '#10b981';
    btnWsConnect.disabled = true;
    btnWsDisconnect.disabled = false;
    btnWsPing.disabled = false;
    btnWsSub.disabled = false;
    btnWsUnsub.disabled = false;
    btnWsSend.disabled = false;
    addWsFeedItem('OPEN', 'HANDSHAKE', 'Full-duplex WebSocket TCP connection established.');
    fetchHealth();
  };

  wsClient.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data);
      addWsFeedItem(msg.type, msg.topic || 'system', msg.payload || msg);
    } catch {
      addWsFeedItem('RAW', '', event.data);
    }
  };

  wsClient.onclose = (event) => {
    wsStatusText.textContent = 'Disconnected';
    document.getElementById('pill-ws').style.borderColor = 'var(--border-color)';
    btnWsConnect.disabled = false;
    btnWsDisconnect.disabled = true;
    btnWsPing.disabled = true;
    btnWsSub.disabled = true;
    btnWsUnsub.disabled = true;
    btnWsSend.disabled = true;
    addWsFeedItem('CLOSED', 'CODE: ' + event.code, 'Connection cleanly closed by peer.');
    fetchHealth();
  };

  wsClient.onerror = (err) => {
    addWsFeedItem('ERROR', '', 'WebSocket error encountered.');
  };
});

btnWsDisconnect?.addEventListener('click', () => {
  if (wsClient) {
    wsClient.close(1000, 'User initiated disconnect');
  }
});

btnWsPing?.addEventListener('click', () => {
  if (wsClient && wsClient.readyState === WebSocket.OPEN) {
    const t0 = performance.now();
    wsClient.send(JSON.stringify({ type: 'PING', timestamp: Date.now() }));
    addWsFeedItem('SENT', 'PING', 'Heartbeat probe dispatched.');
  }
});

btnWsSub?.addEventListener('click', () => {
  const topic = document.getElementById('ws-topic-select').value;
  if (wsClient && wsClient.readyState === WebSocket.OPEN) {
    wsClient.send(JSON.stringify({ type: 'SUBSCRIBE', topic }));
    addWsFeedItem('SUBSCRIBE', topic, `Subscribed to topic "${topic}"`);
  }
});

btnWsUnsub?.addEventListener('click', () => {
  const topic = document.getElementById('ws-topic-select').value;
  if (wsClient && wsClient.readyState === WebSocket.OPEN) {
    wsClient.send(JSON.stringify({ type: 'UNSUBSCRIBE', topic }));
    addWsFeedItem('UNSUBSCRIBE', topic, `Unsubscribed from topic "${topic}"`);
  }
});

btnWsSend?.addEventListener('click', () => {
  const input = document.getElementById('ws-chat-input');
  const text = input.value.trim();
  if (text && wsClient && wsClient.readyState === WebSocket.OPEN) {
    wsClient.send(JSON.stringify({
      type: 'BROADCAST',
      topic: 'orders',
      payload: { message: text, user: 'browser_client' }
    }));
    input.value = '';
  }
});

// --- 4. SERVER-SENT EVENTS (SSE) ---
const sseFeed = document.getElementById('sse-event-feed');
const btnSseConnect = document.getElementById('btn-sse-connect');
const btnSseDisconnect = document.getElementById('btn-sse-disconnect');
const sseStatusText = document.getElementById('sse-status-text');
const sseStateVal = document.getElementById('sse-state-val');
const sseCountVal = document.getElementById('sse-count-val');
const sseLastId = document.getElementById('sse-last-id');

function addSseFeedItem(event, id, data) {
  const item = document.createElement('div');
  item.className = 'feed-item sse';
  item.innerHTML = `
    <div class="feed-item-header">
      <span style="font-weight:600; color:#fbbf24;">[EVENT: ${event}] &bull; ID: ${id || 'none'}</span>
      <span>${new Date().toLocaleTimeString()}</span>
    </div>
    <div class="feed-item-body">${typeof data === 'object' ? JSON.stringify(data) : data}</div>
  `;
  sseFeed.prepend(item);
}

document.getElementById('btn-clear-sse')?.addEventListener('click', () => {
  sseFeed.innerHTML = '';
});

btnSseConnect?.addEventListener('click', () => {
  addSseFeedItem('CONNECTING', '', 'Opening EventSource stream to /api/v1/events/stream');
  sseClient = new EventSource('/api/v1/events/stream');

  sseClient.onopen = () => {
    sseStatusText.textContent = 'Active (Streaming)';
    document.getElementById('pill-sse').style.borderColor = '#f59e0b';
    sseStateVal.textContent = 'OPEN (ReadyState: 1)';
    sseStateVal.style.color = '#10b981';
    btnSseConnect.disabled = true;
    btnSseDisconnect.disabled = false;
    fetchHealth();
  };

  sseClient.addEventListener('connected', (e) => {
    sseEventCount++;
    sseCountVal.textContent = sseEventCount;
    sseLastId.textContent = e.lastEventId || 'init';
    addSseFeedItem('connected', e.lastEventId, JSON.parse(e.data));
  });

  sseClient.addEventListener('order_created', (e) => {
    sseEventCount++;
    sseCountVal.textContent = sseEventCount;
    sseLastId.textContent = e.lastEventId;
    addSseFeedItem('order_created', e.lastEventId, JSON.parse(e.data));
  });

  sseClient.addEventListener('price_tick', (e) => {
    sseEventCount++;
    sseCountVal.textContent = sseEventCount;
    sseLastId.textContent = e.lastEventId;
    addSseFeedItem('price_tick', e.lastEventId, JSON.parse(e.data));
  });

  sseClient.addEventListener('heartbeat', (e) => {
    sseEventCount++;
    sseCountVal.textContent = sseEventCount;
    sseLastId.textContent = e.lastEventId;
    addSseFeedItem('heartbeat', e.lastEventId, JSON.parse(e.data));
  });

  sseClient.onerror = () => {
    sseStateVal.textContent = 'RECONNECTING... (ReadyState: 0)';
    sseStateVal.style.color = '#ef4444';
  };
});

btnSseDisconnect?.addEventListener('click', () => {
  if (sseClient) {
    sseClient.close();
    sseClient = null;
    sseStatusText.textContent = 'Disconnected';
    document.getElementById('pill-sse').style.borderColor = 'var(--border-color)';
    sseStateVal.textContent = 'CLOSED';
    sseStateVal.style.color = '#9ca3af';
    btnSseConnect.disabled = false;
    btnSseDisconnect.disabled = true;
    addSseFeedItem('DISCONNECTED', '', 'Stream closed by client.');
    fetchHealth();
  }
});

// --- 5. HMAC WEBHOOK DISPATCHER & INSPECTOR ---
const webhookConsole = document.getElementById('webhook-console');
const webhookBadge = document.getElementById('webhook-status-badge');

// Browser HMAC SHA-256 calculation helper using SubtleCrypto
async function computeBrowserHMAC(secret, message) {
  const enc = new TextEncoder();
  const key = await crypto.subtle.importKey(
    'raw',
    enc.encode(secret),
    { name: 'HMAC', hash: 'SHA-256' },
    false,
    ['sign']
  );
  const signature = await crypto.subtle.sign('HMAC', key, enc.encode(message));
  return Array.from(new Uint8Array(signature))
    .map((b) => b.toString(16).padStart(2, '0'))
    .join('');
}

document.getElementById('btn-send-webhook')?.addEventListener('click', async () => {
  webhookConsole.textContent = 'Computing HMAC SHA-256 Signature and dispatching...\n';
  const secret = document.getElementById('webhook-secret').value.trim();
  const payloadStr = document.getElementById('webhook-payload').value.trim();
  const tamperMode = document.getElementById('webhook-tamper-select').value;

  let timestamp = Date.now();
  if (tamperMode === 'replay_expired') {
    // Set timestamp to 10 minutes ago
    timestamp = Date.now() - 10 * 60 * 1000;
  }

  const signaturePayload = `${timestamp}.${payloadStr}`;
  let signature = await computeBrowserHMAC(secret, signaturePayload);

  if (tamperMode === 'tamper_sig') {
    signature = signature.substring(0, signature.length - 8) + 'deadbeef';
  }

  const start = performance.now();
  try {
    const res = await fetch('/api/v1/webhooks/incoming', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Hub-Signature-256': `sha256=${signature}`,
        'X-Hub-Timestamp': timestamp.toString(),
      },
      body: payloadStr,
    });

    const duration = (performance.now() - start).toFixed(1);
    const data = await res.json();

    webhookBadge.textContent = `${res.status} ${res.statusText}`;
    webhookBadge.style.background = res.ok
      ? 'rgba(16, 185, 129, 0.3)'
      : 'rgba(239, 68, 68, 0.3)';

    webhookConsole.textContent = [
      `HTTP/1.1 ${res.status} ${res.statusText} (${duration}ms)`,
      `X-Hub-Signature-256: sha256=${signature.substring(0, 24)}...`,
      `X-Hub-Timestamp: ${timestamp} (${new Date(timestamp).toISOString()})`,
      `Tamper Mode: ${tamperMode}`,
      '-------------------------------------------------------',
      JSON.stringify(data, null, 2),
    ].join('\n');
  } catch (err) {
    webhookConsole.textContent = 'Webhook Dispatch Error: ' + err.message;
  }
});

document.getElementById('btn-get-outbound-deliveries')?.addEventListener('click', async () => {
  webhookConsole.textContent = 'Fetching outbound deliveries audit log...\n';
  try {
    const res = await fetch('/api/v1/webhooks/deliveries');
    const data = await res.json();
    webhookConsole.textContent = JSON.stringify(data, null, 2);
  } catch (err) {
    webhookConsole.textContent = 'Error: ' + err.message;
  }
});
