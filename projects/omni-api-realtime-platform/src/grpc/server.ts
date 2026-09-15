import path from 'path';
import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';
import { store } from '../common/store';

const PROTO_PATH = path.resolve(__dirname, '../../specs/service.proto');

const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
  keepCase: true,
  longs: String,
  enums: String,
  defaults: true,
  oneofs: true,
});

const protoDescriptor = grpc.loadPackageDefinition(packageDefinition) as any;
const omniProto = protoDescriptor.enterprise.omni.v1;

export function startGrpcServer(port: number = 50051): grpc.Server {
  const server = new grpc.Server();

  server.addService(omniProto.OmniService.service, {
    // 1. Unary RPC
    GetOrder: (call: any, callback: any) => {
      const orderId = call.request.order_id;
      const order = store.getOrder(orderId);

      if (!order) {
        return callback({
          code: grpc.status.NOT_FOUND,
          message: `Order with ID "${orderId}" was not found.`,
        });
      }

      callback(null, {
        order_id: order.id,
        customer_id: order.customerId,
        items: order.items.map((i) => ({
          product_id: i.productId,
          quantity: i.quantity,
          unit_price: i.unitPrice,
        })),
        total_amount: order.totalAmount,
        currency: order.currency,
        status: order.status === 'PROCESSING' ? 2 : order.status === 'COMPLETED' ? 3 : 1,
        created_at: {
          seconds: Math.floor(new Date(order.createdAt).getTime() / 1000),
          nanos: 0,
        },
        version: order.version,
      });
    },

    // 2. Server Streaming RPC
    StreamOrderStatus: (call: any) => {
      const orderId = call.request.order_id;
      const intervalSec = call.request.heartbeat_interval_seconds || 3;
      console.log(`[gRPC] Starting status stream for order: ${orderId}`);

      // Send initial event
      call.write({
        order_id: orderId,
        status: 1, // PENDING
        update_reason: 'Stream connected. Tracking order lifecycle.',
        event_timestamp: {
          seconds: Math.floor(Date.now() / 1000),
          nanos: 0,
        },
      });

      let step = 0;
      const statuses = [2, 2, 3]; // PROCESSING, PROCESSING, COMPLETED
      const reasons = ['Warehouse inventory reserved', 'Package handed to courier', 'Delivered at destination'];

      const timer = setInterval(() => {
        if (call.cancelled) {
          clearInterval(timer);
          return;
        }

        if (step < statuses.length) {
          call.write({
            order_id: orderId,
            status: statuses[step],
            update_reason: reasons[step],
            event_timestamp: {
              seconds: Math.floor(Date.now() / 1000),
              nanos: 0,
            },
          });
          step++;
        } else {
          clearInterval(timer);
          call.end();
        }
      }, intervalSec * 1000);

      call.on('cancelled', () => {
        clearInterval(timer);
        console.log(`[gRPC] Client cancelled stream for order: ${orderId}`);
      });
    },

    // 3. Client Streaming RPC
    UploadTelemetry: (call: any, callback: any) => {
      let count = 0;
      let totalCpu = 0;
      let deviceId = 'unknown';
      const startTime = Date.now();

      call.on('data', (event: any) => {
        count++;
        totalCpu += event.cpu_usage || 0;
        deviceId = event.device_id || deviceId;
      });

      call.on('end', () => {
        const avgCpu = count > 0 ? Math.round((totalCpu / count) * 100) / 100 : 0;
        const duration = Date.now() - startTime;

        callback(null, {
          device_id: deviceId,
          total_events_processed: count,
          average_cpu_usage: avgCpu,
          processing_duration_ms: duration,
        });
      });

      call.on('error', (err: any) => {
        console.error('[gRPC] Error in UploadTelemetry:', err);
      });
    },

    // 4. Bidirectional Streaming RPC
    LiveSupportSession: (call: any) => {
      call.on('data', (msg: any) => {
        console.log(`[gRPC Bidi] Support message received from ${msg.sender_id}: ${msg.content}`);

        // Echo response back with bot acknowledgment
        call.write({
          session_id: msg.session_id,
          sender_id: 'Enterprise Support Bot',
          content: `ACK [Seq ${Date.now()}]: We received "${msg.content}". Agent standing by.`,
          sent_at: {
            seconds: Math.floor(Date.now() / 1000),
            nanos: 0,
          },
        });
      });

      call.on('end', () => {
        call.end();
      });
    },
  });

  server.bindAsync(
    `0.0.0.0:${port}`,
    grpc.ServerCredentials.createInsecure(),
    (err, boundPort) => {
      if (err) {
        console.error('[gRPC] Failed to bind gRPC server:', err);
        return;
      }
      console.log(`[gRPC] Server running on 0.0.0.0:${boundPort}`);
    }
  );

  return server;
}
