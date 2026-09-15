/**
 * Outbound Webhook Delivery Engine
 * Dispatches webhooks with cryptographic HMAC signatures, replay prevention headers,
 * and exponential backoff retries with full audit history tracking.
 */
import axios from 'axios';
import { db, WebhookEndpoint, WebhookDeliveryRecord } from './db';
import { signWebhookPayload } from './security';

interface DispatchOptions {
  maxRetries?: number;
  initialBackoffMs?: number;
}

export class WebhookDispatcher {
  /**
   * Dispatches an event to all active endpoints subscribed to this event type
   */
  public async dispatchEvent(event: string, payload: any, options: DispatchOptions = {}) {
    const endpoints = db.listWebhookEndpoints().filter((ep) => ep.active && ep.events.includes(event));
    const maxRetries = options.maxRetries ?? 3;
    const initialBackoff = options.initialBackoffMs ?? 500;

    const deliveryPromises = endpoints.map(async (endpoint) => {
      const deliveryRecordId = `del_${Date.now()}_${Math.random().toString(36).substr(2, 6)}`;
      const timestampSeconds = Math.floor(Date.now() / 1000);
      const serializedPayload = JSON.stringify({
        id: deliveryRecordId,
        event,
        created_at: new Date().toISOString(),
        data: payload,
      });

      const signature = signWebhookPayload(serializedPayload, timestampSeconds);

      let attempts = 0;
      let lastStatusCode = 0;
      let lastResponse = '';
      let delivered = false;

      while (attempts < maxRetries && !delivered) {
        attempts++;
        const startTime = Date.now();

        try {
          const response = await axios.post(endpoint.url, serializedPayload, {
            headers: {
              'Content-Type': 'application/json',
              'User-Agent': 'OmniPlatform-Webhook-Engine/1.0',
              'X-Webhook-Signature': signature,
              'X-Webhook-Timestamp': timestampSeconds.toString(),
              'X-Webhook-Event': event,
              'X-Delivery-Attempt': attempts.toString(),
            },
            timeout: 5000,
          });

          lastStatusCode = response.status;
          lastResponse = typeof response.data === 'string' ? response.data : JSON.stringify(response.data);
          delivered = response.status >= 200 && response.status < 300;
        } catch (err: any) {
          lastStatusCode = err.response?.status || 504;
          lastResponse = err.message || 'Connection failed';
        }

        const durationMs = Date.now() - startTime;

        if (!delivered && attempts < maxRetries) {
          // Exponential backoff with jitter: 2^attempt * initialBackoff + random jitter
          const backoff = Math.pow(2, attempts - 1) * initialBackoff + Math.floor(Math.random() * 200);
          console.warn(
            `[Webhook Retry] Endpoint ${endpoint.url} failed with ${lastStatusCode}. Retrying attempt ${attempts + 1}/${maxRetries} in ${backoff}ms...`
          );
          await new Promise((resolve) => setTimeout(resolve, backoff));
        }
      }

      const record: WebhookDeliveryRecord = {
        id: deliveryRecordId,
        endpointId: endpoint.id,
        event,
        payload: serializedPayload,
        responseStatus: lastStatusCode,
        responseBody: lastResponse.slice(0, 1000), // Truncate body
        attempts,
        delivered,
        createdAt: new Date(),
      };

      db.recordDelivery(record);

      if (delivered) {
        console.log(`[Webhook Delivery Success] ${event} delivered to ${endpoint.url} in ${attempts} attempt(s).`);
      } else {
        console.error(`[Webhook DLQ] Failed to deliver ${event} to ${endpoint.url} after ${attempts} attempts.`);
      }

      return record;
    });

    return Promise.all(deliveryPromises);
  }
}

export const webhookDispatcher = new WebhookDispatcher();
