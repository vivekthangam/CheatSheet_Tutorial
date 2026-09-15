import path from 'path';
import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';

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

async function runGrpcTestSuite() {
  const target = process.env.GRPC_TARGET || 'localhost:50051';
  console.log(`[gRPC Client] Connecting to ${target}...`);

  const client = new omniProto.OmniService(
    target,
    grpc.credentials.createInsecure()
  );

  console.log('\n=================== 1. UNARY RPC TEST ===================');
  client.GetOrder({ order_id: 'ord_initial_1' }, (err: any, response: any) => {
    if (err) {
      console.error('[gRPC Unary Error]:', err.message);
    } else {
      console.log('[gRPC Unary Success] Received Order:', response);
    }
  });

  // Small delay to keep logs organized
  await new Promise((r) => setTimeout(r, 1000));

  console.log('\n=================== 2. SERVER STREAMING RPC TEST ===================');
  const serverStream = client.StreamOrderStatus({
    order_id: 'ord_initial_1',
    heartbeat_interval_seconds: 1,
  });

  serverStream.on('data', (event: any) => {
    console.log('[gRPC Server Stream Event]:', event);
  });

  serverStream.on('end', () => {
    console.log('[gRPC Server Stream Complete]');
  });

  await new Promise((r) => setTimeout(r, 4000));

  console.log('\n=================== 3. CLIENT STREAMING RPC TEST ===================');
  const clientStream = client.UploadTelemetry((err: any, summary: any) => {
    if (err) {
      console.error('[gRPC Client Stream Error]:', err);
    } else {
      console.log('[gRPC Client Stream Summary Received]:', summary);
    }
  });

  for (let i = 1; i <= 5; i++) {
    clientStream.write({
      device_id: 'iot_edge_gateway_42',
      sequence_number: i,
      cpu_usage: 35.5 + i * 4.2,
      memory_usage: 62.1,
      timestamp: { seconds: Math.floor(Date.now() / 1000), nanos: 0 },
    });
  }
  clientStream.end();

  await new Promise((r) => setTimeout(r, 1500));

  console.log('\n=================== 4. BIDIRECTIONAL STREAMING RPC TEST ===================');
  const bidiStream = client.LiveSupportSession();

  bidiStream.on('data', (reply: any) => {
    console.log('[gRPC Bidi Reply Received]:', reply.content);
  });

  bidiStream.write({
    session_id: 'sess_991',
    sender_id: 'TestUser_Alice',
    content: 'Hello, I have an issue with my order shipment.',
    sent_at: { seconds: Math.floor(Date.now() / 1000), nanos: 0 },
  });

  setTimeout(() => {
    bidiStream.write({
      session_id: 'sess_991',
      sender_id: 'TestUser_Alice',
      content: 'Can you please check the tracking number?',
      sent_at: { seconds: Math.floor(Date.now() / 1000), nanos: 0 },
    });
    setTimeout(() => {
      bidiStream.end();
      console.log('\n[gRPC Test Suite Completed Successfully]\n');
    }, 1000);
  }, 1000);
}

if (require.main === module) {
  runGrpcTestSuite().catch(console.error);
}

export { runGrpcTestSuite };
