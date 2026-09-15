/**
 * Server-Sent Events (SSE) Stream Manager
 * Delivers unidirectional real-time updates over standard HTTP/1.1 or HTTP/2
 * with proxy-buffering bypass headers, auto-reconnect hints, and keepalive comments.
 */
import { Request, Response } from 'express';

interface SSEClient {
  id: string;
  res: Response;
  topics: Set<string>;
  connectedAt: Date;
}

export class SSEStreamManager {
  private clients: Map<string, SSEClient> = new Map();
  private heartbeatTimer: NodeJS.Timeout | null = null;
  private messageCounter = 0;

  constructor() {
    this.startKeepaliveComments(15000);
  }

  public handleConnection(req: Request, res: Response) {
    const clientId = `sse_${Date.now()}_${Math.random().toString(36).substr(2, 6)}`;

    // Critical headers for SSE and Proxy bypass
    res.writeHead(200, {
      'Content-Type': 'text/event-stream',
      'Cache-Control': 'no-cache, no-transform',
      'Connection': 'keep-alive',
      'X-Accel-Buffering': 'no', // Disables NGINX buffering
      'Access-Control-Allow-Origin': '*',
    });

    // Send initial client reconnection instruction (5 seconds backoff)
    res.write('retry: 5000\n');
    res.write(`: Connected client ${clientId}\n\n`);

    const topicsParam = (req.query.topics as string) || 'all';
    const topics = new Set<string>(topicsParam.split(',').map((t) => t.trim()));

    const client: SSEClient = {
      id: clientId,
      res,
      topics,
      connectedAt: new Date(),
    };

    this.clients.set(clientId, client);
    console.log(`[SSE] Client connected: ${clientId}. Subscribed topics: [${Array.from(topics).join(', ')}]`);

    // Handle client disconnect
    req.on('close', () => {
      this.clients.delete(clientId);
      console.log(`[SSE] Client closed connection: ${clientId}. Active: ${this.clients.size}`);
    });
  }

  public broadcast(topic: string, eventName: string, data: any) {
    this.messageCounter++;
    const payload = JSON.stringify(data);
    const message = `id: ${this.messageCounter}\nevent: ${eventName}\ndata: ${payload}\n\n`;

    let deliveredCount = 0;
    for (const [id, client] of this.clients.entries()) {
      if (client.topics.has('all') || client.topics.has(topic)) {
        client.res.write(message);
        deliveredCount++;
      }
    }
    return deliveredCount;
  }

  /**
   * Heartbeat comments to prevent intermediate proxies, ALB, and cloud NAT from dropping idle streams
   */
  private startKeepaliveComments(intervalMs: number) {
    this.heartbeatTimer = setInterval(() => {
      const comment = `: keepalive-ping ${Date.now()}\n\n`;
      for (const client of this.clients.values()) {
        try {
          client.res.write(comment);
        } catch (err) {
          // Socket write failed
        }
      }
    }, intervalMs);
  }

  public close() {
    if (this.heartbeatTimer) clearInterval(this.heartbeatTimer);
    for (const client of this.clients.values()) {
      client.res.end();
    }
    this.clients.clear();
  }

  public getActiveCount(): number {
    return this.clients.size;
  }
}

export const sseManager = new SSEStreamManager();
