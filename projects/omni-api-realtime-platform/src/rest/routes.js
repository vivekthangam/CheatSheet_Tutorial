import { Router } from 'express';
import crypto from 'node:crypto';
import { store } from '../store/dataStore.js';
import { idempotencyMiddleware } from './idempotency.js';

export const restRouter = Router();

// 1. GET /api/v1/products - With ETag & Cache-Control
restRouter.get('/products', (req, res) => {
  const { category, maxPrice } = req.query;
  const products = store.getProducts({
    category,
    maxPrice: maxPrice ? parseFloat(maxPrice) : undefined,
  });

  // Generate deterministic ETag from payload hash
  const payloadString = JSON.stringify(products);
  const hash = crypto.createHash('md5').update(payloadString).digest('hex');
  const etag = `W/"${hash}"`;

  res.setHeader('ETag', etag);
  res.setHeader('Cache-Control', 'public, max-age=60');

  // Conditional request validation
  const clientEtag = req.headers['if-none-match'];
  if (clientEtag && clientEtag === etag) {
    // 304 Not Modified - Save network bandwidth & CPU
    return res.status(304).end();
  }

  return res.json({
    data: products,
    total: products.length,
    cachedAt: new Date().toISOString(),
  });
});

// 2. GET /api/v1/products/:id
restRouter.get('/products/:id', (req, res) => {
  const product = store.getProductById(req.params.id);
  if (!product) {
    return res.status(404).json({ error: `Product ${req.params.id} not found` });
  }
  return res.json(product);
});

// 3. POST /api/v1/orders - With Idempotency Guard
restRouter.post('/orders', idempotencyMiddleware, (req, res) => {
  const { customerId, items } = req.body;

  if (!items || !Array.isArray(items) || items.length === 0) {
    return res.status(400).json({ error: 'Order must contain at least one item' });
  }

  try {
    const order = store.createOrder({ customerId, items });
    return res.status(201).json({
      success: true,
      message: 'Order placed successfully',
      order,
    });
  } catch (err) {
    return res.status(400).json({ error: err.message });
  }
});

// 4. GET /api/v1/orders
restRouter.get('/orders', (req, res) => {
  const limit = req.query.limit ? parseInt(req.query.limit, 10) : 20;
  const orders = store.getOrders(limit);
  return res.json({
    data: orders,
    total: orders.length,
  });
});

// 5. GET /api/v1/metrics
restRouter.get('/metrics', (req, res) => {
  const products = store.getProducts();
  const orders = store.getOrders();
  const deliveries = store.getWebhookDeliveries();

  return res.json({
    timestamp: new Date().toISOString(),
    uptimeSeconds: process.uptime(),
    memoryUsageMB: +(process.memoryUsage().rss / (1024 * 1024)).toFixed(2),
    counts: {
      products: products.length,
      orders: orders.length,
      sseSubscribers: store.sseClients.size,
      webhookDeliveries: deliveries.length,
      idempotencyKeysCached: store.idempotencyCache.size,
    },
  });
});

// 6. GET /api/v1/webhooks/deliveries
restRouter.get('/webhooks/deliveries', (req, res) => {
  const deliveries = store.getWebhookDeliveries();
  return res.json({ data: deliveries });
});
