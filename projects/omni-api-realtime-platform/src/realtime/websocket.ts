import { Server as HttpServer } from 'http';
import { WebSocketServer, WebSocket } from 'ws';
import { store } from '../common/store';
import { EventEnvelope } from '../common/envelope';

interface ExtendedWebSocket extends WebSocket {
  isAlive: boolean;
  subscriptions: Set<string>;
  clientId: string;
}

export function setupWebSocketServer(httpServer: HttpServer): WebSocketServer {
  const wss = new WebSocketServer({ noServer: true });

  // 1. Connection lifecycle
  wss.on('connection', (ws: ExtendedWebSocket) => {
    ws.isAlive = true;
    ws.subscriptions = new Set(['all', 'orders']);
    ws.clientId = `ws_${Math.random().toString(36).substring(2, 9)}`;

    console.log(`[WebSocket] Client connected: ${ws.clientId}`);

    // Send welcome envelope
    ws.send(
      JSON.stringify({
        type: 'CONNECTION_ACK',
        clientId: ws.clientId,
        subscribedChannels: Array.from(ws.subscriptions),
        timestamp: Date.now(),
      })
    );

    // RFC 6455 Pong handler for liveness
    ws.on('pong', () => {
      ws.isAlive = true;
    });

    // Client incoming message handling
    ws.on('message', (raw) => {
      try {
        const message = JSON.parse(raw.toString());
        switch (message.type) {
          case 'SUBSCRIBE': {
            const chan = message.channel || message.topic;
            if (chan) {
              ws.subscriptions.add(chan);
              ws.send(JSON.stringify({ type: 'SUBSCRIBED', topic: chan, channel: chan }));
            }
            break;
          }
          case 'UNSUBSCRIBE': {
            const chan = message.channel || message.topic;
            if (chan) {
              ws.subscriptions.delete(chan);
              ws.send(JSON.stringify({ type: 'UNSUBSCRIBED', topic: chan, channel: chan }));
            }
            break;
          }
          case 'PING':
            ws.send(JSON.stringify({ type: 'PONG', timestamp: Date.now() }));
            break;
          case 'BROADCAST':
            // Broadcast user message to peer clients
            broadcastToClients(wss, {
              header: {
                eventId: `msg_${Date.now()}`,
                eventType: 'user.broadcast',
                schemaVersion: '1.2.0',
                timestamp: Date.now(),
                correlationId: `corr_${ws.clientId}`,
                traceId: ws.clientId,
                senderId: ws.clientId,
                sequenceNumber: 0,
              },
              payload: message.payload || message.data,
            });
            break;
          default:
            ws.send(JSON.stringify({ type: 'ERROR', message: 'Unknown packet type' }));
        }
      } catch (err) {
        ws.send(JSON.stringify({ type: 'ERROR', message: 'Malformed JSON framing' }));
      }
    });

    ws.on('close', (code, reason) => {
      console.log(`[WebSocket] Client ${ws.clientId} disconnected. Code: ${code}, Reason: ${reason.toString()}`);
    });
  });

  // 2. Broadcast Platform Store events to subscribed WebSocket peers
  store.on('event', (envelope: EventEnvelope) => {
    broadcastToClients(wss, envelope);
  });

  // 3. RFC 6455 Liveness Heartbeat check (30s interval)
  const interval = setInterval(() => {
    wss.clients.forEach((client) => {
      const ws = client as ExtendedWebSocket;
      if (ws.isAlive === false) {
        console.warn(`[WebSocket] Terminating zombie socket: ${ws.clientId}`);
        return ws.terminate();
      }
      ws.isAlive = false;
      ws.ping(); // Send RFC 6455 control ping frame
    });
  }, 30000);

  wss.on('close', () => {
    clearInterval(interval);
  });

  return wss;
}

function broadcastToClients(wss: WebSocketServer, envelope: EventEnvelope) {
  const payload = JSON.stringify({
    type: 'EVENT_ENVELOPE',
    envelope,
  });

  wss.clients.forEach((client) => {
    const ws = client as ExtendedWebSocket;
    if (ws.readyState === WebSocket.OPEN) {
      // Check channel subscription
      if (ws.subscriptions.has('all') || ws.subscriptions.has('orders')) {
        ws.send(payload);
      }
    }
  });
}
