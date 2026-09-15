import { store } from '../store/dataStore.js';

/**
 * Server-Sent Events (SSE) Handler with Last-Event-ID recovery & heartbeat
 */
export function sseHandler(req, res) {
  // Set required SSE headers
  res.writeHead(200, {
    'Content-Type': 'text/event-stream',
    'Cache-Control': 'no-cache, no-transform',
    'Connection': 'keep-alive',
    'X-Accel-Buffering': 'no', // Disables proxy buffering (Nginx, Envoy)
    'Access-Control-Allow-Origin': '*',
  });

  // Flush headers immediately if response supports it
  if (typeof res.flushHeaders === 'function') {
    res.flushHeaders();
  }

  // Register client with dataStore
  const sseClient = store.registerSseClient(res);

  // Send initial handshake and retry advisory (5000ms)
  res.write(`retry: 5000\n`);
  res.write(`event: connected\n`);
  res.write(`data: ${JSON.stringify({ clientId: sseClient.id, connectedAt: new Date().toISOString() })}\n\n`);

  // Handle client reconnection replay if Last-Event-ID header is provided
  const lastEventId = req.headers['last-event-id'];
  if (lastEventId) {
    const replayEvents = store.getEventsSince(lastEventId);
    for (const evt of replayEvents) {
      res.write(`id: ${evt.id}\n`);
      res.write(`event: ${evt.topic || 'message'}\n`);
      res.write(`data: ${JSON.stringify(evt)}\n\n`);
    }
  }

  // SSE Keep-Alive heartbeat comment line every 15 seconds
  const heartbeatInterval = setInterval(() => {
    res.write(`: keep-alive ping ${Date.now()}\n\n`);
  }, 15000);

  // Cleanup on connection disconnect
  req.on('close', () => {
    clearInterval(heartbeatInterval);
    store.removeSseClient(res);
  });
}
