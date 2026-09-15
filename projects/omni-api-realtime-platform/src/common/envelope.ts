import { v4 as uuidv4 } from 'uuid';

/**
 * Enterprise Universal Event Message Envelope Standard
 * Conforms to modern distributed event-driven architectural standards:
 * - Monotonic sequence numbers for dropped-frame detection
 * - UUIDv7/v4 event identifiers
 * - W3C distributed tracing context (traceId, correlationId)
 * - Schema version governance
 */
export interface EventHeader {
  eventId: string;
  eventType: string;
  schemaVersion: string;
  timestamp: number;
  correlationId: string;
  traceId: string;
  senderId: string;
  sequenceNumber: number;
}

export interface EventEnvelope<T = any> {
  header: EventHeader;
  payload: T;
  metadata?: Record<string, any>;
}

let globalSequence = 0;

export function createEnvelope<T>(
  eventType: string,
  payload: T,
  options?: {
    correlationId?: string;
    traceId?: string;
    senderId?: string;
    metadata?: Record<string, any>;
  }
): EventEnvelope<T> {
  globalSequence += 1;
  return {
    header: {
      eventId: uuidv4(),
      eventType,
      schemaVersion: '1.2.0',
      timestamp: Date.now(),
      correlationId: options?.correlationId || `corr_${uuidv4().substring(0, 8)}`,
      traceId: options?.traceId || uuidv4().replace(/-/g, ''),
      senderId: options?.senderId || 'omni-platform-core',
      sequenceNumber: globalSequence,
    },
    payload,
    metadata: options?.metadata,
  };
}
