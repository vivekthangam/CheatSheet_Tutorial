/**
 * Enterprise Security Engine
 * Implements Token Bucket Rate Limiting, GraphQL Depth/Complexity guards,
 * JWT authentication, and HMAC-SHA256 signature verification for Webhooks.
 */
import crypto from 'crypto';
import jwt from 'jsonwebtoken';
import { Request, Response, NextFunction } from 'express';
import { DocumentNode, FieldNode, OperationDefinitionNode } from 'graphql';

const JWT_SECRET = process.env.JWT_SECRET || 'omni-platform-super-secret-key-prod-2026';
const WEBHOOK_SIGNING_SECRET = process.env.WEBHOOK_SECRET || 'whsec_9942a77f0a82b45e9981d3f2c5';

// ---------------------------------------------------------------------------
// 1. Sliding Window / Token Bucket Rate Limiter
// ---------------------------------------------------------------------------
interface TokenBucket {
  tokens: number;
  lastRefill: number;
}

export class RateLimiter {
  private buckets: Map<string, TokenBucket> = new Map();
  private maxTokens: number;
  private refillRatePerSec: number;

  constructor(maxTokens = 100, refillRatePerSec = 20) {
    this.maxTokens = maxTokens;
    this.refillRatePerSec = refillRatePerSec;
  }

  public allowRequest(clientId: string, cost = 1): { allowed: boolean; remainingTokens: number; resetSec: number } {
    const now = Date.now();
    let bucket = this.buckets.get(clientId);

    if (!bucket) {
      bucket = { tokens: this.maxTokens, lastRefill: now };
      this.buckets.set(clientId, bucket);
    } else {
      const elapsedSec = (now - bucket.lastRefill) / 1000;
      bucket.tokens = Math.min(this.maxTokens, bucket.tokens + elapsedSec * this.refillRatePerSec);
      bucket.lastRefill = now;
    }

    if (bucket.tokens >= cost) {
      bucket.tokens -= cost;
      return {
        allowed: true,
        remainingTokens: Math.floor(bucket.tokens),
        resetSec: Math.ceil((this.maxTokens - bucket.tokens) / this.refillRatePerSec),
      };
    }

    return {
      allowed: false,
      remainingTokens: Math.floor(bucket.tokens),
      resetSec: Math.ceil((cost - bucket.tokens) / this.refillRatePerSec),
    };
  }

  public middleware() {
    return (req: Request, res: Response, next: NextFunction) => {
      const clientIp = (req.headers['x-forwarded-for'] as string) || req.socket.remoteAddress || 'unknown-client';
      const result = this.allowRequest(clientIp, 1);

      res.setHeader('X-RateLimit-Limit', this.maxTokens);
      res.setHeader('X-RateLimit-Remaining', result.remainingTokens);
      res.setHeader('X-RateLimit-Reset', result.resetSec);

      if (!result.allowed) {
        res.status(429).json({
          error: 'TOO_MANY_REQUESTS',
          message: 'Rate limit quota exceeded. Please back off.',
          retryAfterSeconds: result.resetSec,
        });
        return;
      }
      next();
    };
  }
}

// ---------------------------------------------------------------------------
// 2. GraphQL Query Complexity & Depth Calculator
// ---------------------------------------------------------------------------
export function calculateQueryDepth(node: any, currentDepth = 0): number {
  if (!node || !node.selectionSet) return currentDepth;
  let maxChildDepth = currentDepth;
  for (const selection of node.selectionSet.selections) {
    if (selection.kind === 'Field') {
      const childDepth = calculateQueryDepth(selection, currentDepth + 1);
      if (childDepth > maxChildDepth) maxChildDepth = childDepth;
    }
  }
  return maxChildDepth;
}

export function validateQueryComplexity(document: DocumentNode, maxDepth = 6): void {
  const operations = document.definitions.filter(
    (def) => def.kind === 'OperationDefinition'
  ) as OperationDefinitionNode[];

  for (const op of operations) {
    const depth = calculateQueryDepth(op, 0);
    if (depth > maxDepth) {
      throw new Error(`GraphQL Query rejected: Maximum depth ${maxDepth} exceeded (received query depth: ${depth}).`);
    }
  }
}

// ---------------------------------------------------------------------------
// 3. JWT Authentication Verification
// ---------------------------------------------------------------------------
export interface TokenPayload {
  userId: string;
  role: 'USER' | 'ADMIN' | 'SERVICE';
  tier: 'FREE' | 'PRO' | 'ENTERPRISE';
}

export function signToken(payload: TokenPayload, expiresIn = '2h'): string {
  return jwt.sign(payload, JWT_SECRET, { expiresIn });
}

export function verifyToken(token: string): TokenPayload | null {
  try {
    return jwt.verify(token, JWT_SECRET) as TokenPayload;
  } catch (err) {
    return null;
  }
}

// ---------------------------------------------------------------------------
// 4. Webhook Cryptographic Signing (HMAC-SHA256) & Replay Prevention
// ---------------------------------------------------------------------------
export function signWebhookPayload(payloadString: string, timestamp: number): string {
  const signaturePayload = `t=${timestamp},v1=${payloadString}`;
  return crypto.createHmac('sha256', WEBHOOK_SIGNING_SECRET).update(signaturePayload).digest('hex');
}

export function verifyWebhookSignature(
  payloadString: string,
  signatureHeader: string,
  timestampHeader: string,
  maxToleranceSeconds = 300
): boolean {
  const timestamp = parseInt(timestampHeader, 10);
  if (isNaN(timestamp)) return false;

  const nowSeconds = Math.floor(Date.now() / 1000);
  if (Math.abs(nowSeconds - timestamp) > maxToleranceSeconds) {
    console.warn(`[Security Alert] Webhook timestamp outside allowed clock-skew window (${timestamp})`);
    return false;
  }

  const expectedSignature = signWebhookPayload(payloadString, timestamp);
  return crypto.timingSafeEqual(Buffer.from(signatureHeader), Buffer.from(expectedSignature));
}
