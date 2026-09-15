/**
 * Production WebSocket Connection & Pub/Sub Manager
 * Implements RFC 6455 compliant heartbeats, zombie connection reaping,
 * and topic-based multiplexing with authentication.
 */
import { WebSocketServer, WebSocket } from 'ws';
import { IncomingMessage } from 'http';
import { verifyToken, TokenPayload } from './security';

interface ClientConnection {
  ws: WebSocket;
  isAlive: boolean;
  subscriptions: Set<string>;
  clientInfo?: TokenPayload;
  connectedAt: Date;
}

export class RealtimeWebSocketManager {
  private wss: WebSocketServer;
  private clients: Map<WebSocket, ClientConnection> = new Map();
  private heartbeatTimer: NodeJS.Timeout | null = null;

  constructor(server: any, path = '/ws') {
    this.wss = new WebSocketServer({ server, path });
    this.setupListeners();
    this.startHeartbeat(30000); // 30s ping interval
  }

  private setupListeners() {
    this.wss.on('connection', (ws: WebSocket, req: IncomingMessage) => {
      // Optional query token authentication: /ws?token=...
      const url = new URL(req.url || '', `http://${req.headers.host || 'localhost'}`);
      const token = url.searchParams.get('token');
      let clientInfo: TokenPayload | undefined;

      if (token) {
        const payload = verifyToken(token);
        if (payload) {
          clientInfo = payload;
        }
      }

      const connection: ClientConnection = {
        ws,
        isAlive: true,
        subscriptions: new Set<string>(),
        clientInfo,
        connectedAt: new Date(),
      };

      this.clients.set(ws, connection);
      console.log(`[WS] Client connected. Total active connections: ${this.clients.size}`);

      // Setup RFC 6455 Pong listener
      ws.on('pong', () => {
        const conn = this.clients.get(ws);
        if (conn) conn.isAlive = true;
      });

      ws.on('message', (data: Buffer | string) => {
        try {
          const message = JSON.parse(data.toString());
          this.handleClientMessage(connection, message);
        } catch (err: any) {
          ws.send(JSON.stringify({ type: 'ERROR', code: 'INVALID_JSON', message: err.message }));
        }
      });

      ws.on('close', (code, reason) => {
        this.clients.delete(ws);
        console.log(`[WS] Client disconnected (code: ${code}, reason: ${reason}). Active: ${this.clients.size}`);
      });

      ws.on('error', (err) => {
        console.error('[WS] Socket error:', err);
        ws.terminate();
        this.clients.delete(ws);
      });

      // Send initial handshake acknowledgment
      ws.send(JSON.stringify({
        type: 'CONNECTION_ACK',
        serverTime: new Date().toISOString(),
        authenticated: !!clientInfo,
        userId: clientInfo?.userId || 'anonymous',
      }));
    });
  }

  private handleClientMessage(conn: ClientConnection, msg: any) {
    switch (msg.action) {
      case 'SUBSCRIBE':
        if (typeof msg.topic === 'string') {
          conn.subscriptions.add(msg.topic);
          conn.ws.send(JSON.stringify({ type: 'SUBSCRIBED', topic: msg.topic }));
        }
        break;

      case 'UNSUBSCRIBE':
        if (typeof msg.topic === 'string') {
          conn.subscriptions.delete(msg.topic);
          conn.ws.send(JSON.stringify({ type: 'UNSUBSCRIBED', topic: msg.topic }));
        }
        break;

      case 'PING':
        conn.ws.send(JSON.stringify({ type: 'PONG', timestamp: Date.now() }));
        break;

      default:
        conn.ws.send(JSON.stringify({ type: 'ERROR', message: `Unknown action: ${msg.action}` }));
    }
  }

  /**
   * Broadcast an event payload to all clients subscribed to a specific topic
   */
  public broadcastToTopic(topic: string, event: string, payload: any) {
    const serialized = JSON.stringify({
      topic,
      event,
      timestamp: new Date().toISOString(),
      data: payload,
    });

    let recipientCount = 0;
    for (const [ws, conn] of this.clients.entries()) {
      if (conn.subscriptions.has(topic) && ws.readyState === WebSocket.OPEN) {
        ws.send(serialized);
        recipientCount++;
      }
    }
    return recipientCount;
  }

  /**
   * Zombie connection detection via RFC 6455 Ping/Pong
   */
  private startHeartbeat(intervalMs: number) {
    this.heartbeatTimer = setInterval(() => {
      for (const [ws, conn] of this.clients.entries()) {
        if (!conn.isAlive) {
          console.warn(`[WS Heartbeat] Terminating zombie connection from ${conn.clientInfo?.userId || 'anonymous'}`);
          ws.terminate();
          this.clients.delete(ws);
          continue;
        }

        conn.isAlive = false; // Will be set to true on pong
        ws.ping();
      }
    }, intervalMs);
  }

  public close() {
    if (this.heartbeatTimer) clearInterval(this.heartbeatTimer);
    this.wss.close();
  }

  public getActiveCount(): number {
    return this.clients.size;
  }
}
