import express, { Request, Response, NextFunction } from 'express';
import http from 'http';
import path from 'path';
import { graphql } from 'graphql';

import { restRouter } from './rest/routes';
import { swaggerRouter } from './rest/swagger';
import { sseRouter } from './realtime/sse';
import { setupWebSocketServer } from './realtime/websocket';
import { setupSocketIOServer } from './realtime/socketio';
import { startGrpcServer } from './grpc/server';
import { getGraphQLSchema, resolvers, createGraphQLContext } from './graphql/schema';
import { store } from './common/store';

const app = express();
const httpServer = http.createServer(app);

// 1. Native Enterprise CORS Middleware (Zero external dependencies)
app.use((req: Request, res: Response, next: NextFunction) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, PATCH, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, Idempotency-Key, If-None-Match');
  if (req.method === 'OPTIONS') {
    return res.status(200).end();
  }
  next();
});

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// 2. Health & System Probe Endpoint
app.get('/health', (_req: Request, res: Response) => {
  const products = store.getProducts();
  const orders = store.getOrders();

  res.json({
    status: 'HEALTHY',
    version: '2.0.0',
    protocols: {
      rest: { status: 'ONLINE', endpoints: ['/api/v1/products', '/api/v1/orders'] },
      graphql: { status: 'ONLINE', endpoint: '/graphql' },
      grpc: { status: 'ONLINE', port: 50051 },
      sse: { status: 'ONLINE', streamUrl: '/api/v1/events/stream' },
      websocket: { status: 'ONLINE', url: 'ws://localhost:4000/ws' },
      socketio: { status: 'ONLINE', path: '/socket.io' },
    },
    metrics: {
      uptimeSeconds: Math.floor(process.uptime()),
      totalProducts: products.length,
      totalOrders: orders.totalCount,
      memoryUsageMB: Math.round((process.memoryUsage().rss / (1024 * 1024)) * 10) / 10,
    },
    timestamp: new Date().toISOString(),
  });
});

// 3. Serve Live Dashboard static files
app.use(express.static(path.resolve(__dirname, '../public')));

// 4. Mount REST API endpoints
app.use('/api/v1', restRouter);

// 5. Mount SSE streams (both /api/v1/events/stream and /api/stream/events)
app.use('/api/v1/events/stream', (req: Request, res: Response, next: NextFunction) => {
  req.url = '/events';
  sseRouter(req, res, next);
});
app.use('/api/stream', sseRouter);

// 6. Mount Swagger UI & OpenAPI 3.1 Documentation
app.use('/docs', swaggerRouter);

// 7. Mount GraphQL Engine with DataLoader
app.all('/graphql', async (req: Request, res: Response) => {
  const query = req.method === 'POST' ? req.body?.query : (req.query.query as string);
  const variables = req.method === 'POST' ? req.body?.variables : req.query.variables;
  const operationName = req.method === 'POST' ? req.body?.operationName : (req.query.operationName as string);

  if (!query) {
    return res.status(400).json({ errors: [{ message: 'Missing GraphQL query parameter or body.' }] });
  }

  try {
    const parsedVariables = typeof variables === 'string' ? JSON.parse(variables) : variables;
    const result = await graphql({
      schema: getGraphQLSchema(),
      source: query,
      rootValue: {
        ...resolvers.Query,
        ...resolvers.Mutation,
      },
      contextValue: createGraphQLContext(),
      variableValues: parsedVariables,
      operationName,
    });

    return res.json(result);
  } catch (err: any) {
    return res.status(500).json({ errors: [{ message: err.message || 'GraphQL Execution Failed' }] });
  }
});

// 8. Mount Real-Time Servers
const wss = setupWebSocketServer(httpServer);
setupSocketIOServer(httpServer);

// 9. WebSocket HTTP Upgrade Router
httpServer.on('upgrade', (request, socket, head) => {
  const pathname = new URL(request.url || '', `http://${request.headers.host}`).pathname;
  if (pathname === '/ws') {
    wss.handleUpgrade(request, socket, head, (ws) => {
      wss.emit('connection', ws, request);
    });
  }
});

// 10. Start Servers
const HTTP_PORT = parseInt(process.env.PORT || '4000', 10);
const GRPC_PORT = parseInt(process.env.GRPC_PORT || '50051', 10);

httpServer.listen(HTTP_PORT, () => {
  console.log(`\n================================================================`);
  console.log(`🚀 Omni-Protocol Realtime Platform is Live!`);
  console.log(`----------------------------------------------------------------`);
  console.log(`📊 Interactive Dashboard:  http://localhost:${HTTP_PORT}`);
  console.log(`📄 Swagger UI / OpenAPI:   http://localhost:${HTTP_PORT}/docs/swagger`);
  console.log(`🌐 REST API Base:          http://localhost:${HTTP_PORT}/api/v1`);
  console.log(`⚡ GraphQL Endpoint:       http://localhost:${HTTP_PORT}/graphql`);
  console.log(`📡 Server-Sent Events:     http://localhost:${HTTP_PORT}/api/v1/events/stream`);
  console.log(`🔌 Raw WebSocket (RFC 6455): ws://localhost:${HTTP_PORT}/ws`);
  console.log(`🔄 Socket.IO Server:       http://localhost:${HTTP_PORT}/socket.io`);
  console.log(`📦 gRPC Server:            0.0.0.0:${GRPC_PORT}`);
  console.log(`================================================================\n`);
});

startGrpcServer(GRPC_PORT);

export { app, httpServer };
