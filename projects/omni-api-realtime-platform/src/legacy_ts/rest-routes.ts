/**
 * Enterprise REST API Routes
 * Implements strict HTTP semantics, idempotency keys, ETag conditional caching,
 * role-based access control, and webhook ingestion verification.
 */
import { Router, Request, Response } from 'express';
import crypto from 'crypto';
import { db, Product, Order } from './db';
import {
  authenticateJWT,
  requireRole,
  generateToken,
  verifyWebhookSignature,
  signWebhookPayload,
} from './security';
import { sseManager } from './sse-handler';
import { webhookDispatcher } from './webhook-dispatcher';

export const restRouter = Router();

// In-memory Idempotency Store (key -> response payload)
const idempotencyStore = new Map<string, { status: number; body: any }>();

// -------------------------------------------------------------
// Authentication Endpoints
// -------------------------------------------------------------
restRouter.post('/auth/login', (req: Request, res: Response) => {
  const { email } = req.body;
  if (!email) {
    return res.status(400).json({ error: 'Missing email field' });
  }

  const user = db.listUsers().find((u) => u.email.toLowerCase() === email.toLowerCase());
  if (!user) {
    return res.status(401).json({ error: 'Invalid credentials. Try alice@example.com or admin@enterprise.com' });
  }

  const token = generateToken({
    userId: user.id,
    email: user.email,
    role: user.role,
    tier: user.tier,
  });

  return res.json({
    message: 'Authentication successful',
    token,
    user: {
      id: user.id,
      name: user.name,
      email: user.email,
      role: user.role,
      tier: user.tier,
    },
  });
});

restRouter.get('/auth/me', authenticateJWT, (req: any, res: Response) => {
  const user = db.getUser(req.user.userId);
  if (!user) return res.status(404).json({ error: 'User not found' });
  return res.json({ user });
});

// -------------------------------------------------------------
// Products API (with ETag conditional 304 caching)
// -------------------------------------------------------------
restRouter.get('/products', (req: Request, res: Response) => {
  const { category, minPrice, maxPrice, limit = '20', offset = '0' } = req.query;

  let products = db.listProducts();
  if (category) {
    products = products.filter((p) => p.category.toLowerCase() === (category as string).toLowerCase());
  }
  if (minPrice) {
    products = products.filter((p) => p.price >= parseFloat(minPrice as string));
  }
  if (maxPrice) {
    products = products.filter((p) => p.price <= parseFloat(maxPrice as string));
  }

  const paginated = products.slice(parseInt(offset as string), parseInt(offset as string) + parseInt(limit as string));

  // Compute weak ETag from response content
  const hash = crypto.createHash('md5').update(JSON.stringify(paginated)).digest('hex');
  const etag = `W/"${hash}"`;

  res.setHeader('ETag', etag);
  res.setHeader('Cache-Control', 'public, max-age=60, must-revalidate');

  if (req.headers['if-none-match'] === etag) {
    // 304 Not Modified
    return res.status(304).end();
  }

  return res.json({
    data: paginated,
    total: products.length,
    limit: parseInt(limit as string),
    offset: parseInt(offset as string),
  });
});

restRouter.get('/products/:id', (req: Request, res: Response) => {
  const product = db.getProduct(req.params.id);
  if (!product) return res.status(404).json({ error: `Product ${req.params.id} not found` });
  return res.json({ product });
});

restRouter.post('/products', authenticateJWT, requireRole('ADMIN'), (req: Request, res: Response) => {
  const { name, category, price, stock } = req.body;
  if (!name || !category || price === undefined || stock === undefined) {
    return res.status(400).json({ error: 'Missing required product fields: name, category, price, stock' });
  }

  const newProduct: Product = {
    id: `prod_${Date.now()}_${Math.random().toString(36).substr(2, 4)}`,
    name,
    sku: `SKU-${Math.floor(1000 + Math.random() * 9000)}`,
    category,
    price: Number(price),
    stock: Number(stock),
    reviews: [],
  };

  db.saveProduct(newProduct);

  const wsManager = (global as any).wsManager;
  if (wsManager) {
    wsManager.broadcastToTopic('inventory', 'PRODUCT_CREATED', newProduct);
  }
  sseManager.broadcast('inventory', 'product_created', newProduct);

  return res.status(201).json({ product: newProduct });
});

// -------------------------------------------------------------
// Orders API (with Idempotency-Key support)
// -------------------------------------------------------------
restRouter.get('/orders', authenticateJWT, (req: any, res: Response) => {
  const { status, limit = '20', offset = '0' } = req.query;
  let orders = db.listOrders();

  // Non-admins can only see their own orders
  if (req.user.role !== 'ADMIN') {
    orders = orders.filter((o) => o.userId === req.user.userId);
  }

  if (status) {
    orders = orders.filter((o) => o.status === (status as string).toUpperCase());
  }

  return res.json({
    data: orders.slice(parseInt(offset as string), parseInt(offset as string) + parseInt(limit as string)),
    total: orders.length,
  });
});

restRouter.post('/orders', authenticateJWT, async (req: any, res: Response) => {
  const idempotencyKey = req.headers['idempotency-key'] as string;

  // Check if idempotency key was previously processed
  if (idempotencyKey && idempotencyStore.has(idempotencyKey)) {
    const cached = idempotencyStore.get(idempotencyKey)!;
    res.setHeader('X-Cache-Lookup', 'IDEMPOTENT_HIT');
    return res.status(cached.status).json(cached.body);
  }

  const { items } = req.body;
  if (!Array.isArray(items) || items.length === 0) {
    return res.status(400).json({ error: 'Items array cannot be empty' });
  }

  try {
    let totalAmount = 0;
    const orderItems = [];

    // Stock verification
    for (const item of items) {
      const product = db.getProduct(item.productId);
      if (!product) return res.status(404).json({ error: `Product ${item.productId} not found` });
      if (product.stock < item.quantity) {
        return res.status(409).json({
          error: `Insufficient stock for ${product.name} (have: ${product.stock}, want: ${item.quantity})`,
        });
      }
      totalAmount += product.price * item.quantity;
      orderItems.push({
        productId: product.id,
        quantity: item.quantity,
        unitPrice: product.price,
      });
    }

    // Deduct stock
    for (const item of items) {
      db.updateStock(item.productId, -item.quantity);
    }

    const newOrder: Order = {
      id: `ord_${Date.now()}_${Math.random().toString(36).substr(2, 4)}`,
      userId: req.user.userId,
      items: orderItems,
      totalAmount: Math.round(totalAmount * 100) / 100,
      status: 'PROCESSING',
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    db.createOrder(newOrder);

    // Cross-Protocol Realtime Event Broadcasts
    const wsManager = (global as any).wsManager;
    if (wsManager) {
      wsManager.broadcastToTopic('orders', 'ORDER_CREATED', newOrder);
      wsManager.broadcastToTopic(`user:${req.user.userId}`, 'ORDER_UPDATE', newOrder);
    }
    sseManager.broadcast('orders', 'order_created', newOrder);
    webhookDispatcher.dispatchEvent('order.created', newOrder);

    const responseBody = { order: newOrder };

    // Save in idempotency cache if key provided
    if (idempotencyKey) {
      idempotencyStore.set(idempotencyKey, { status: 201, body: responseBody });
    }

    return res.status(201).json(responseBody);
  } catch (err: any) {
    return res.status(500).json({ error: err.message });
  }
});

// -------------------------------------------------------------
// Webhooks API
// -------------------------------------------------------------
restRouter.post('/webhooks/endpoints', authenticateJWT, requireRole('ADMIN'), (req: Request, res: Response) => {
  const { url, events } = req.body;
  if (!url || !Array.isArray(events)) {
    return res.status(400).json({ error: 'Must provide valid url and events array' });
  }

  const endpoint = db.registerWebhookEndpoint(url, events);
  return res.status(201).json({ endpoint });
});

restRouter.get('/webhooks/endpoints', authenticateJWT, requireRole('ADMIN'), (req: Request, res: Response) => {
  return res.json({ endpoints: db.listWebhookEndpoints() });
});

restRouter.get('/webhooks/deliveries', authenticateJWT, requireRole('ADMIN'), (req: Request, res: Response) => {
  return res.json({ deliveries: db.listDeliveries() });
});

/**
 * Inbound Webhook Listener / Signature Verifier Test Endpoint
 * Allows testing HMAC-SHA256 signature verification and replay prevention.
 */
restRouter.post('/webhooks/inbound-test', (req: Request, res: Response) => {
  const signature = req.headers['x-webhook-signature'] as string;
  const timestampStr = req.headers['x-webhook-timestamp'] as string;

  if (!signature || !timestampStr) {
    return res.status(400).json({
      verified: false,
      error: 'Missing required security headers: X-Webhook-Signature or X-Webhook-Timestamp',
    });
  }

  const timestamp = parseInt(timestampStr, 10);
  const rawPayload = JSON.stringify(req.body);

  const verification = verifyWebhookSignature(rawPayload, signature, timestamp, 300);

  if (!verification.valid) {
    return res.status(401).json({
      verified: false,
      reason: verification.reason,
    });
  }

  return res.json({
    verified: true,
    message: 'Webhook HMAC signature verified successfully. Replay protection passed.',
    receivedEvent: req.headers['x-webhook-event'] || 'unknown',
    data: req.body,
  });
});

// -------------------------------------------------------------
// Realtime SSE Stream Endpoint
// -------------------------------------------------------------
restRouter.get('/events/stream', (req: Request, res: Response) => {
  sseManager.handleConnection(req, res);
});
