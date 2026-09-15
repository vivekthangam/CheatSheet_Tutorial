/**
 * Production WebSocket Server & Topic Pub/Sub Manager
 * Implements bidirectional heartbeat detection (Ping/Pong), connection lifecycle hooks,
 * topic filtering, and token authentication.
 */
import { Server as HTTPServer } from 'http';
import { WebSocketServer, WebSocket } from 'ws';
import { verifyToken } from './security';

interface ExtendedWebSocket extends WebSocket {
  id: string;
  isAlive: boolean;
  subscriptions: Set<string>;
  userId?: string;
  userRole?: string;
}

export class WSManager {
  private wss: WebSocketServer;
  private pingInterval: NodeJS.Timeout | null = null;
  private clientCounter = 0;

  constructor(server: HTTPServer) {
    this.wss = new WebSocketServer({ server, path: '/ws' });
    this.initialize();
  }

  private initialize() {
    this.wss.on('connection', (ws: ExtendedWebSocket, req) => {
      this.clientCounter++;
      ws.id = `ws_${Date.now()}_${this.clientCounter}`;
      ws.isAlive = true;
      ws.subscriptions = new Set<string>(['system']); // Default channel

      console.log(`[WebSocket] Client connected: ${ws.id} from ${req.socket.remoteAddress}`);

      // Setup Heartbeat listener
      ws.on('pong', () => {
        ws.isAlive = true;
      });

      // Handle Incoming Messages
      ws.on('message', (raw: Buffer) => {
        this.handleClientMessage(ws, raw.toString());
      });

      // Handle Disconnection
      ws.on('close', (code, reason) => {
        console.log(`[WebSocket] Client closed: ${ws.id} (code: ${code}, reason: ${reason.toString() || 'none'})`);
      });

      ws.on('error', (err) => {
        console.error(`[WebSocket] Socket error on ${ws.id}:`, err);
      });

      // Welcome handshake packet
      this.sendToSocket(ws, {
        type: 'CONNECTION_ACK',
        clientId: ws.id,
        serverTime: new Date().toISOString(),
        defaultSubscriptions: Array.from(ws.subscriptions),
        availableTopics: ['orders', 'inventory', 'system', 'metrics'],
      });
    });

    // 30-Second Ping/Pong Audit Loop to terminate ghost sockets
    this.pingInterval = setInterval(() => {
      this.wss.clients.forEach((client) => {
        const ws = client as ExtendedWebSocket;
        if (ws.isAlive === false) {
          console.warn(`[WebSocket] Terminating inactive ghost socket: ${ws.id}`);
          return ws.terminate();
        }
        ws.isAlive = false;
        ws.ping();
      });
    }, 30000);
  }

  private handleClientMessage(ws: ExtendedWebSocket, messageStr: string) {
    try {
      const data = JSON.parse(messageStr);
      const { type, topic, token, payload } = data;

      switch (type) {
        case 'AUTH': {
          if (!token) {
            this.sendError(ws, 'Missing token in AUTH packet');
            return;
          }
          const decoded = verifyToken(token);
          if (!decoded) {
            this.sendError(ws, 'Invalid or expired JWT token');
            return;
          }
          ws.userId = decoded.userId;
          ws.userRole = decoded.role;
          // Auto-subscribe to user's private channel
          ws.subscriptions.add(`user:${decoded.userId}`);
          this.sendToSocket(ws, {
            type: 'AUTH_SUCCESS',
            userId: decoded.userId,
            role: decoded.role,
            subscriptions: Array.from(ws.subscriptions),
          });
          break;
        }

        case 'SUBSCRIBE': {
          if (!topic) {
            this.sendError(ws, 'Must specify topic to subscribe');
            return;
          }
          ws.subscriptions.add(topic);
          this.sendToSocket(ws, {
            type: 'SUBSCRIPTION_ACK',
            subscribedTopic: topic,
            currentSubscriptions: Array.from(ws.subscriptions),
          });
          break;
        }

        case 'UNSUBSCRIBE': {
          if (!topic) {
            this.sendError(ws, 'Must specify topic to unsubscribe');
            return;
          }
          ws.subscriptions.delete(topic);
          this.sendToSocket(ws, {
            type: 'UNSUBSCRIPTION_ACK',
            unsubscribedTopic: topic,
            currentSubscriptions: Array.from(ws.subscriptions),
          });
          break;
        }

        case 'PING': {
          this.sendToSocket(ws, { type: 'PONG', timestamp: Date.now() });
          break;
        }

        case 'BROADCAST_CHAT': {
          // Relay peer chat across system channel
          this.broadcastToTopic('system', 'CHAT_MESSAGE', {
            sender: ws.userId || ws.id,
            text: payload?.text || '',
            timestamp: new Date().toISOString(),
          });
          break;
        }

        default:
          this.sendError(ws, `Unknown message type: ${type}`);
      }
    } catch (err: any) {
      this.sendError(ws, `Malformed JSON packet: ${err.message}`);
    }
  }

  public broadcastToTopic(topic: string, event: string, data: any) {
    const packet = JSON.stringify({
      topic,
      event,
      timestamp: new Date().toISOString(),
      data,
    });

    let count = 0;
    this.wss.clients.forEach((client) => {
      const ws = client as ExtendedWebSocket;
      if (ws.readyState === WebSocket.OPEN && (ws.subscriptions.has('all') || ws.subscriptions.has(topic))) {
        ws.send(packet);
        count++;
      }
    });
    return count;
  }

  public sendToSocket(ws: WebSocket, obj: any) {
    if (ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify(obj));
    }
  }

  private sendError(ws: WebSocket, error: string) {
    this.sendToSocket(ws, { type: 'ERROR', error, timestamp: Date.now() });
  }

  public getActiveCount(): number {
    return this.wss.clients.size;
  }

  public close() {
    if (this.pingInterval) clearInterval(this.pingInterval);
    this.wss.close();
  }
}
