import crypto from 'crypto';
import http from 'http';
import https from 'https';
import { store } from '../store/dataStore.js';

// In-memory registry of webhook subscriptions
let webhookSubscriptions = [
  {
    id: 'wh_sub_local_demo',
    url: 'http://localhost:3000/api/webhooks/test-receiver',
    secret: 'whsec_demo_supersecret_key_849204',
    topics: ['orders', 'inventory'],
    createdAt: new Date().toISOString(),
    active: true,
  },
];

// Delivery logs store
let deliveryLogs = [];

/**
 * Generate HMAC-SHA256 signature
 */
export function generateSignature(payloadString, secret) {
  return 'sha256=' + crypto.createHmac('sha256', secret).update(payloadString).digest('hex');
}

/**
 * Deliver webhook event with exponential backoff retries
 */
export async function deliverWebhook(sub, event, maxAttempts = 3) {
  const payloadString = JSON.stringify(event);
  const signature = generateSignature(payloadString, sub.secret);
  const timestamp = Date.now().toString();

  const urlObj = new URL(sub.url);
  const isHttps = urlObj.protocol === 'https:';
  const clientLib = isHttps ? https : http;

  const logEntry = {
    id: `deliv_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
    subscriptionId: sub.id,
    targetUrl: sub.url,
    eventTopic: event.topic,
    timestamp: new Date().toISOString(),
    signature,
    attempts: 0,
    status: 'PENDING',
    statusCode: null,
    latencyMs: 0,
    error: null,
  };

  const startTime = Date.now();

  for (let attempt = 1; attempt <= maxAttempts; attempt++) {
    logEntry.attempts = attempt;
    try {
      const response = await new Promise((resolve, reject) => {
        const req = clientLib.request(
          sub.url,
          {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
              'Content-Length': Buffer.byteLength(payloadString),
              'X-Hub-Signature-256': signature,
              'X-Webhook-Timestamp': timestamp,
              'X-Webhook-Event': event.topic,
              'User-Agent': 'Omni-Webhook-Dispatcher/1.0',
            },
            timeout: 5000,
          },
          (res) => {
            let body = '';
            res.on('data', (chunk) => (body += chunk));
            res.on('end', () => resolve({ statusCode: res.statusCode, body }));
          }
        );

        req.on('error', reject);
        req.on('timeout', () => {
          req.destroy(new Error('Request timeout after 5000ms'));
        });

        req.write(payloadString);
        req.end();
      });

      logEntry.latencyMs = Date.now() - startTime;
      logEntry.statusCode = response.statusCode;

      if (response.statusCode >= 200 && response.statusCode < 300) {
        logEntry.status = 'SUCCESS';
        break;
      } else {
        logEntry.status = 'FAILED';
        logEntry.error = `HTTP error ${response.statusCode}: ${response.body.slice(0, 120)}`;
      }
    } catch (err) {
      logEntry.latencyMs = Date.now() - startTime;
      logEntry.status = 'FAILED';
      logEntry.error = err.message;
    }

    if (logEntry.status === 'FAILED' && attempt < maxAttempts) {
      // Exponential backoff wait
      await new Promise((r) => setTimeout(r, Math.pow(2, attempt) * 200));
    }
  }

  deliveryLogs.unshift(logEntry);
  if (deliveryLogs.length > 50) deliveryLogs.pop();
  return logEntry;
}

/**
 * Initialize Webhook dispatcher listener
 */
export function initWebhookDispatcher() {
  store.on('realtime_event', (event) => {
    const matchedSubs = webhookSubscriptions.filter(
      (sub) => sub.active && (sub.topics.includes(event.topic) || sub.topics.includes('*'))
    );

    for (const sub of matchedSubs) {
      deliverWebhook(sub, event).catch((err) => {
        console.error(`Webhook delivery error to ${sub.url}:`, err.message);
      });
    }
  });
}

// In-memory test receiver log for demo purposes
let receivedWebhookLogs = [];

export const webhookRouter = {
  getSubscriptions: () => webhookSubscriptions,
  addSubscription: ({ url, secret, topics }) => {
    const newSub = {
      id: `wh_sub_${Date.now()}`,
      url,
      secret: secret || 'whsec_' + crypto.randomBytes(12).toString('hex'),
      topics: topics && topics.length ? topics : ['*'],
      createdAt: new Date().toISOString(),
      active: true,
    };
    webhookSubscriptions.push(newSub);
    return newSub;
  },
  deleteSubscription: (id) => {
    webhookSubscriptions = webhookSubscriptions.filter((s) => s.id !== id);
    return { success: true, id };
  },
  getDeliveryLogs: () => deliveryLogs,
  getReceivedLogs: () => receivedWebhookLogs,
  handleTestReceiver: (req, res) => {
    const sig = req.headers['x-hub-signature-256'];
    const timestamp = req.headers['x-webhook-timestamp'];
    const eventType = req.headers['x-webhook-event'];

    const entry = {
      id: `rcv_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
      receivedAt: new Date().toISOString(),
      eventType,
      signature: sig,
      timestamp,
      payload: req.body,
    };

    receivedWebhookLogs.unshift(entry);
    if (receivedWebhookLogs.length > 30) receivedWebhookLogs.pop();

    return res.status(200).json({ status: 'ACCEPTED', receivedId: entry.id });
  },
};
