import { WebSocketServer, WebSocket } from 'ws';
import { store } from '../store/dataStore.js';

/**
 * Enterprise WebSocket Server with topic-based pub/sub and heartbeats
 */
export function initWebSocketServer(server) {
  const wss = new WebSocketServer({ server, path: '/ws' });

  // Map each client to their subscribed topic Set and liveness state
  const clientSubscriptions = new Map();

  wss.on('connection', (ws, req) => {
    const clientId = `ws_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`;
    const subscriptions = new Set(['metrics']); // Default subscription
    clientSubscriptions.set(ws, { id: clientId, subscriptions, isAlive: true });

    // Send welcome frame
    ws.send(
      JSON.stringify({
        type: 'CONNECTED',
        clientId,
        subscribedTopics: Array.from(subscriptions),
        availableTopics: ['orders', 'metrics', 'inventory'],
        message: 'Connected to Omni-API WebSocket Gateway. Send {"type":"SUBSCRIBE","topic":"orders"} to subscribe.',
        timestamp: new Date().toISOString(),
      })
    );

    ws.on('pong', () => {
      const clientMeta = clientSubscriptions.get(ws);
      if (clientMeta) clientMeta.isAlive = true;
    });

    ws.on('message', (raw) => {
      try {
        const msg = JSON.parse(raw.toString());
        const clientMeta = clientSubscriptions.get(ws);

        if (!clientMeta) return;

        switch (msg.type) {
          case 'SUBSCRIBE':
            if (msg.topic) {
              clientMeta.subscriptions.add(msg.topic);
              ws.send(
                JSON.stringify({
                  type: 'SUBSCRIPTION_CONFIRMED',
                  topic: msg.topic,
                  allSubscriptions: Array.from(clientMeta.subscriptions),
                })
              );
            }
            break;

          case 'UNSUBSCRIBE':
            if (msg.topic) {
              clientMeta.subscriptions.delete(msg.topic);
              ws.send(
                JSON.stringify({
                  type: 'UNSUBSCRIPTION_CONFIRMED',
                  topic: msg.topic,
                  allSubscriptions: Array.from(clientMeta.subscriptions),
                })
              );
            }
            break;

          case 'PING':
            ws.send(JSON.stringify({ type: 'PONG', timestamp: new Date().toISOString() }));
            break;

          case 'BROADCAST':
            // Allow client to push custom notification to peers
            broadcastTopic(msg.topic || 'general', {
              sender: clientMeta.id,
              payload: msg.payload || {},
              timestamp: new Date().toISOString(),
            });
            break;

          default:
            ws.send(JSON.stringify({ error: `Unknown message type: ${msg.type}` }));
        }
      } catch (err) {
        ws.send(JSON.stringify({ error: 'Malformed JSON payload', details: err.message }));
      }
    });

    ws.on('close', () => {
      clientSubscriptions.delete(ws);
    });

    ws.on('error', (err) => {
      console.error(`WebSocket error on client ${clientId}:`, err.message);
    });
  });

  // Function to broadcast event to clients subscribed to its topic
  function broadcastTopic(topic, payload) {
    const frame = JSON.stringify({
      type: 'TOPIC_EVENT',
      topic,
      payload,
      timestamp: new Date().toISOString(),
    });

    for (const [ws, meta] of clientSubscriptions.entries()) {
      if (ws.readyState === WebSocket.OPEN && (meta.subscriptions.has(topic) || meta.subscriptions.has('*'))) {
        ws.send(frame);
      }
    }
  }

  // Hook into dataStore events
  store.on('realtime_event', (event) => {
    broadcastTopic(event.topic || 'general', event);
  });

  // Heartbeat ping/pong interval every 30 seconds to clean up dead sockets
  const heartbeatInterval = setInterval(() => {
    for (const [ws, meta] of clientSubscriptions.entries()) {
      if (!meta.isAlive) {
        ws.terminate();
        clientSubscriptions.delete(ws);
        continue;
      }
      meta.isAlive = false;
      ws.ping();
    }
  }, 30000);

  wss.on('close', () => {
    clearInterval(heartbeatInterval);
  });

  return { wss, broadcastTopic };
}
