/**
 * Omni-Protocol Realtime Platform - Core Server Entrypoint
 * Unifies REST, GraphQL, WebSocket, SSE, and Webhooks into a single cohesive runtime.
 */
import express from 'express';
import http from 'http';
import path from 'path';
import cors from 'cors';
import { graphqlHTTP } from 'express-graphql';
import { schema, rootResolvers } from './graphql-schema';
import { restRouter } from './rest-routes';
import { WSManager } from './ws-handler';
import { sseManager } from './sse-handler';
import { db } from './db';

const app = express();
const server = http.createServer(app);

const PORT = process.env.PORT || 4000;

// Security & Parsing Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Request logging middleware
app.use((req, res, next) => {
  const start = Date.now();
  res.on('finish', () => {
    const duration = Date.now() - start;
    if (!req.path.startsWith('/public') && !req.path.includes('favicon')) {
      console.log(`[HTTP] ${req.method} ${req.originalUrl} -> ${res.statusCode} (${duration}ms)`);
    }
  });
  next();
});

// Initialize WebSocket Manager & attach to global for resolver access
const wsManager = new WSManager(server);
(global as any).wsManager = wsManager;

// Mount REST API Router
app.use('/api/v1', restRouter);

// Mount GraphQL API Endpoint
app.use(
  '/graphql',
  graphqlHTTP((req) => ({
    schema,
    rootValue: rootResolvers,
    graphiql: true, // Interactive GraphiQL IDE
    context: {
      wsManager,
      req,
    },
  }))
);

// Serve static assets for Interactive Realtime Dashboard
app.use(express.static(path.join(__dirname, '../public')));

// Fallback route for dashboard
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, '../public/index.html'));
});

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({
    status: 'UP',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    protocols: {
      rest: 'Active (/api/v1)',
      graphql: 'Active (/graphql)',
      websocket: 'Active (/ws)',
      sse: 'Active (/api/v1/events/stream)',
      webhooks: 'Active (/api/v1/webhooks)',
    },
    metrics: {
      activeWebSockets: wsManager.getActiveCount(),
      activeSSEStreams: sseManager.getActiveCount(),
      totalProducts: db.listProducts().length,
      totalOrders: db.listOrders().length,
    },
  });
});

// Background simulated ticker to stream dynamic market updates to SSE & WS subscribers
const ticker = setInterval(() => {
  const products = db.listProducts();
  if (products.length > 0) {
    const randomProduct = products[Math.floor(Math.random() * products.length)];
    const priceDelta = (Math.random() * 2 - 1).toFixed(2);
    const newPrice = Math.max(10, +(randomProduct.price + parseFloat(priceDelta)).toFixed(2));
    randomProduct.price = newPrice;

    // Push tick to realtime channels
    wsManager.broadcastToTopic('metrics', 'PRICE_TICK', {
      productId: randomProduct.id,
      name: randomProduct.name,
      price: newPrice,
      timestamp: new Date().toISOString(),
    });

    sseManager.broadcast('metrics', 'price_tick', {
      productId: randomProduct.id,
      name: randomProduct.name,
      price: newPrice,
    });
  }
}, 8000);

// Graceful Shutdown
function handleShutdown(signal: string) {
  console.log(`\nReceived ${signal}. Gracefully shutting down...`);
  clearInterval(ticker);
  wsManager.close();
  server.close(() => {
    console.log('HTTP & WebSocket servers closed.');
    process.exit(0);
  });
}

process.on('SIGINT', () => handleShutdown('SIGINT'));
process.on('SIGTERM', () => handleShutdown('SIGTERM'));

// Start server if not running in test mode
if (process.env.NODE_ENV !== 'test') {
  server.listen(PORT, () => {
    console.log('===============================================================');
    console.log('🚀 OMNI-PROTOCOL REALTIME PLATFORM INITIALIZED');
    console.log(`📡 HTTP Server & REST API:   http://localhost:${PORT}/api/v1`);
    console.log(`🔮 GraphQL Endpoint:         http://localhost:${PORT}/graphql`);
    console.log(`⚡ WebSocket Server:          ws://localhost:${PORT}/ws`);
    console.log(`🌊 Server-Sent Events (SSE): http://localhost:${PORT}/api/v1/events/stream`);
    console.log(`🪝 Outbound/Inbound Webhooks: http://localhost:${PORT}/api/v1/webhooks`);
    console.log(`🖥️  Realtime Control UI:      http://localhost:${PORT}/`);
    console.log('===============================================================');
  });
}

export { app, server, wsManager };
