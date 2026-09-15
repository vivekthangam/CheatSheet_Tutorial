import { store } from '../store/dataStore.js';

/**
 * Enterprise Idempotency Middleware
 * Prevents double-charge / duplicate mutations by intercepting requests
 * bearing an 'Idempotency-Key' header.
 */
export function idempotencyMiddleware(req, res, next) {
  const idempotencyKey = req.headers['idempotency-key'];

  if (!idempotencyKey) {
    // If client did not provide idempotency key, proceed normally
    return next();
  }

  const cached = store.idempotencyCache.get(idempotencyKey);

  if (cached) {
    if (cached.status === 'PROCESSING') {
      return res.status(409).json({
        error: 'Conflict: Another request with the same Idempotency-Key is currently being processed.',
        idempotencyKey,
      });
    }

    // Cache HIT: Replay original response verbatim
    res.setHeader('X-Cache', 'HIT (Idempotent replay)');
    res.setHeader('X-Idempotent-Key', idempotencyKey);
    return res.status(cached.status).json(cached.body);
  }

  // Cache MISS: Reserve key in PROCESSING state
  store.idempotencyCache.set(idempotencyKey, {
    status: 'PROCESSING',
    timestamp: Date.now(),
  });

  // Intercept res.json to capture response payload
  const originalJson = res.json.bind(res);
  res.json = (body) => {
    // Save to idempotency cache
    store.idempotencyCache.set(idempotencyKey, {
      status: res.statusCode || 200,
      body,
      timestamp: Date.now(),
    });

    res.setHeader('X-Cache', 'MISS (Processed)');
    res.setHeader('X-Idempotent-Key', idempotencyKey);
    return originalJson(body);
  };

  next();
}
