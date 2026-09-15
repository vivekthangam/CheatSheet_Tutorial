import { Router, Request, Response } from 'express';
import { store } from '../common/store';
import { EventEnvelope } from '../common/envelope';

export const sseRouter = Router();

sseRouter.get('/events', (req: Request, res: Response) => {
  // 1. Establish SSE HTTP Headers
  res.writeHead(200, {
    'Content-Type': 'text/event-stream; charset=utf-8',
    'Cache-Control': 'no-cache, no-transform',
    Connection: 'keep-alive',
    'X-Accel-Buffering': 'no', // Critical: disables Nginx buffering
  });

  // 2. Send initial connection confirmation
  res.write(`: SSE stream connected successfully\n`);
  res.write(`retry: 3000\n\n`);

  // 3. Handle Last-Event-ID resumption
  const lastEventId = req.headers['last-event-id'] as string;
  if (lastEventId) {
    const lastSeq = parseInt(lastEventId, 10);
    if (!isNaN(lastSeq)) {
      const missedEvents = store.getEventsSince(lastSeq);
      console.log(`[SSE] Replaying ${missedEvents.length} missed events since ID ${lastSeq}`);
      missedEvents.forEach((env) => {
        sendSseEnvelope(res, env);
      });
    }
  } else {
    // Send recent initial state
    const recentEvents = store.getAllRecentEvents().slice(-5);
    recentEvents.forEach((env) => {
      sendSseEnvelope(res, env);
    });
  }

  // 4. Listener for live events from PlatformStore
  const eventListener = (envelope: EventEnvelope) => {
    sendSseEnvelope(res, envelope);
  };

  store.on('event', eventListener);

  // 5. Keepalive Heartbeat (every 15 seconds to prevent NAT timeouts)
  const heartbeatTimer = setInterval(() => {
    res.write(`: keepalive ping ${Date.now()}\n\n`);
  }, 15000);

  // 6. Cleanup on client disconnect
  req.on('close', () => {
    clearInterval(heartbeatTimer);
    store.removeListener('event', eventListener);
    console.log('[SSE] Client disconnected from event stream');
  });
});

function sendSseEnvelope(res: Response, env: EventEnvelope) {
  res.write(`id: ${env.header.sequenceNumber}\n`);
  res.write(`event: ${env.header.eventType}\n`);
  res.write(`data: ${JSON.stringify(env)}\n\n`);
}
