import { Server as HttpServer } from 'http';
import { Server as SocketIOServer } from 'socket.io';
import { store } from '../common/store';
import { EventEnvelope } from '../common/envelope';

export function setupSocketIOServer(httpServer: HttpServer): SocketIOServer {
  const io = new SocketIOServer(httpServer, {
    cors: {
      origin: '*',
      methods: ['GET', 'POST'],
    },
    path: '/socket.io',
  });

  // 1. Default Namespace
  io.on('connection', (socket) => {
    console.log(`[Socket.IO] Client connected to default namespace: ${socket.id}`);

    // Join room for targeted broadcasts
    socket.on('join_room', (room: string) => {
      socket.join(room);
      socket.emit('joined_room', { room, status: 'SUCCESS' });
      console.log(`[Socket.IO] Socket ${socket.id} joined room: ${room}`);
    });

    // Request-Response ACK pattern
    socket.on('check_order_status', (orderId: string, ackCallback: (res: any) => void) => {
      const order = store.getOrder(orderId);
      if (order) {
        ackCallback({ status: 'FOUND', order });
      } else {
        ackCallback({ status: 'NOT_FOUND', message: `Order ${orderId} does not exist` });
      }
    });

    // Custom client event
    socket.on('client_message', (msg: any) => {
      io.emit('chat_broadcast', {
        sender: socket.id,
        text: msg.text || msg,
        timestamp: Date.now(),
      });
    });

    socket.on('disconnect', (reason) => {
      console.log(`[Socket.IO] Client ${socket.id} disconnected: ${reason}`);
    });
  });

  // 2. Dedicated Admin Namespace
  const adminNamespace = io.of('/admin');
  adminNamespace.on('connection', (socket) => {
    console.log(`[Socket.IO Admin] Admin connected: ${socket.id}`);

    socket.emit('admin_stats', {
      uptimeSeconds: Math.floor(process.uptime()),
      memoryUsage: process.memoryUsage(),
      totalProducts: store.getProducts().length,
      totalOrders: store.getOrders().totalCount,
    });
  });

  // 3. Forward Platform Store events to Socket.IO clients
  store.on('event', (envelope: EventEnvelope) => {
    io.emit('order_event', envelope);
    if (envelope.header.eventType.startsWith('order.')) {
      const order = envelope.payload;
      if (order && order.id) {
        io.to(`order:${order.id}`).emit('targeted_order_update', envelope);
      }
    }
  });

  return io;
}
