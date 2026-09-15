import { Router, Request, Response } from 'express';
import crypto from 'crypto';
import { store } from '../common/store';
import { sendProblemDetails } from '../common/errors';

export const restRouter = Router();

function generateETag(data: any): string {
  const hash = crypto.createHash('sha256').update(JSON.stringify(data)).digest('hex').substring(0, 16);
  return `W/"${hash}"`;
}

// 1. GET /api/v1/products - Returns catalog with ETag & Cache-Control
restRouter.get('/products', (req: Request, res: Response) => {
  const products = store.getProducts();
  const etag = generateETag(products);

  res.setHeader('ETag', etag);
  res.setHeader('Cache-Control', 'public, max-age=60');

  if (req.headers['if-none-match'] === etag) {
    return res.status(304).end();
  }

  return res.json({
    data: products,
    total: products.length,
  });
});

// 2. GET /api/v1/products/:id
restRouter.get('/products/:id', (req: Request, res: Response) => {
  const product = store.getProduct(req.params.id);
  if (!product) {
    return sendProblemDetails(
      res,
      404,
      'Product Not Found',
      `Product with ID ${req.params.id} does not exist in our catalog.`,
      req.originalUrl,
      'ERR_PRODUCT_NOT_FOUND'
    );
  }

  const etag = generateETag(product);
  res.setHeader('ETag', etag);

  if (req.headers['if-none-match'] === etag) {
    return res.status(304).end();
  }

  return res.json(product);
});

// 3. GET /api/v1/orders - Cursor Pagination & Filtering
restRouter.get('/orders', (req: Request, res: Response) => {
  const limit = parseInt(req.query.limit as string) || 5;
  const after = req.query.cursor as string | undefined;
  const status = req.query.status as string | undefined;

  const { orders, hasNextPage, endCursor, totalCount } = store.getOrders({
    first: limit,
    after,
    filterStatus: status,
  });

  return res.json({
    data: orders,
    pagination: {
      limit,
      hasNextPage,
      nextCursor: endCursor || null,
      totalCount,
    },
    _links: {
      self: req.originalUrl,
      next: endCursor ? `/api/v1/orders?limit=${limit}&cursor=${encodeURIComponent(endCursor)}` : null,
    },
  });
});

// 4. GET /api/v1/orders/:id
restRouter.get('/orders/:id', (req: Request, res: Response) => {
  const order = store.getOrder(req.params.id);
  if (!order) {
    return sendProblemDetails(
      res,
      404,
      'Order Not Found',
      `Order with ID ${req.params.id} does not exist.`,
      req.originalUrl,
      'ERR_ORDER_NOT_FOUND'
    );
  }

  const etag = generateETag(order);
  res.setHeader('ETag', etag);

  if (req.headers['if-none-match'] === etag) {
    return res.status(304).end();
  }

  return res.json(order);
});

// 5. POST /api/v1/orders - Idempotency Key, Validation & Creation
restRouter.post('/orders', (req: Request, res: Response) => {
  const idempotencyKey = req.headers['idempotency-key'] as string;

  if (!idempotencyKey) {
    return sendProblemDetails(
      res,
      400,
      'Missing Idempotency-Key Header',
      'The Idempotency-Key header is required to guarantee safe distributed payment processing.',
      req.originalUrl,
      'ERR_MISSING_IDEMPOTENCY_KEY'
    );
  }

  // Check idempotency cache
  const cached = store.getIdempotency(idempotencyKey);
  if (cached) {
    res.setHeader('X-Cache-Lookup', 'HIT (Idempotent Replay)');
    return res.status(cached.responseStatus).json(cached.responseBody);
  }

  const { customerId, items } = req.body;
  if (!customerId || !Array.isArray(items) || items.length === 0) {
    return sendProblemDetails(
      res,
      422,
      'Unprocessable Order Payload',
      'Order requires a valid customerId and at least one item.',
      req.originalUrl,
      'ERR_INVALID_ORDER_BODY',
      [
        { name: 'customerId', reason: 'Required string' },
        { name: 'items', reason: 'Non-empty array of { productId, quantity } required' },
      ]
    );
  }

  try {
    const order = store.createOrder({ customerId, items });
    const responsePayload = {
      message: 'Order created successfully',
      data: order,
    };

    // Store in idempotency cache
    store.setIdempotency(idempotencyKey, {
      status: 'COMPLETED',
      responseStatus: 201,
      responseBody: responsePayload,
      createdAt: Date.now(),
    });

    res.setHeader('X-Cache-Lookup', 'MISS (Processed)');
    res.setHeader('Location', `/api/v1/orders/${order.id}`);
    return res.status(201).json(responsePayload);
  } catch (err: any) {
    return sendProblemDetails(
      res,
      422,
      'Order Creation Failed',
      err.message || 'Unable to process order.',
      req.originalUrl,
      'ERR_INVENTORY_OR_CUSTOMER'
    );
  }
});

// 6. PATCH /api/v1/orders/:id - Partial Update (Status modification)
restRouter.patch('/orders/:id', (req: Request, res: Response) => {
  const { status } = req.body;
  if (!status) {
    return sendProblemDetails(
      res,
      400,
      'Missing Status Field',
      'Provide a status field in the JSON body.',
      req.originalUrl,
      'ERR_MISSING_FIELD'
    );
  }

  try {
    const updated = store.updateOrderStatus(req.params.id, status);
    return res.json({
      message: 'Order updated',
      data: updated,
    });
  } catch (err: any) {
    return sendProblemDetails(
      res,
      404,
      'Order Update Failed',
      err.message,
      req.originalUrl,
      'ERR_ORDER_NOT_FOUND'
    );
  }
});

// 7. DELETE /api/v1/orders/:id - 204 No Content
restRouter.delete('/orders/:id', (req: Request, res: Response) => {
  const order = store.getOrder(req.params.id);
  if (!order) {
    return sendProblemDetails(
      res,
      404,
      'Order Not Found',
      `Order ${req.params.id} does not exist.`,
      req.originalUrl,
      'ERR_ORDER_NOT_FOUND'
    );
  }

  store.updateOrderStatus(req.params.id, 'CANCELLED');
  return res.status(204).end();
});
