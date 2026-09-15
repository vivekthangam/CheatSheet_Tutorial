import { EventEmitter } from 'node:events';
import crypto from 'node:crypto';

class DataStore extends EventEmitter {
  constructor() {
    super();
    this.products = [
      { id: 'prod_1', name: 'Ultra-Fast NVMe SSD 2TB', category: 'Storage', price: 179.99, stock: 45 },
      { id: 'prod_2', name: 'Ergonomic Mechanical Keyboard', category: 'Peripherals', price: 129.50, stock: 30 },
      { id: 'prod_3', name: '4K IPS Ultra-Wide Monitor 34"', category: 'Displays', price: 599.00, stock: 12 },
      { id: 'prod_4', name: 'Studio USB Condenser Microphone', category: 'Audio', price: 99.00, stock: 25 },
      { id: 'prod_5', name: 'Active Noise Cancelling Headphones', category: 'Audio', price: 249.99, stock: 18 },
      { id: 'prod_6', name: 'Thunderbolt 4 Docking Station', category: 'Accessories', price: 219.00, stock: 8 },
    ];

    this.orders = [
      {
        id: 'ord_1001',
        customerId: 'cust_enterprise_01',
        items: [{ productId: 'prod_1', quantity: 2, unitPrice: 179.99 }],
        totalAmount: 359.98,
        status: 'DELIVERED',
        createdAt: new Date(Date.now() - 3600000).toISOString(),
      },
    ];

    this.idempotencyCache = new Map(); // key -> { status, headers, body, timestamp }
    this.webhookDeliveries = [];
    this.sseClients = new Set();
    this.eventHistory = []; // For Last-Event-ID replay
    this.maxEventHistory = 100;
  }

  // Products
  getProducts(filter = {}) {
    let result = [...this.products];
    if (filter.category) {
      result = result.filter((p) => p.category.toLowerCase() === filter.category.toLowerCase());
    }
    if (filter.maxPrice) {
      result = result.filter((p) => p.price <= filter.maxPrice);
    }
    return result;
  }

  getProductById(id) {
    return this.products.find((p) => p.id === id);
  }

  updateProductPrice(id, newPrice) {
    const product = this.getProductById(id);
    if (product) {
      const oldPrice = product.price;
      product.price = parseFloat(newPrice.toFixed(2));
      const event = {
        type: 'PRICE_UPDATE',
        topic: 'metrics',
        data: { id: product.id, name: product.name, oldPrice, newPrice: product.price, timestamp: new Date().toISOString() },
      };
      this.emitRealtimeEvent(event);
      return product;
    }
    return null;
  }

  // Orders
  createOrder({ customerId, items }) {
    // Validate stock and compute total
    let totalAmount = 0;
    const orderItems = [];

    for (const item of items) {
      const product = this.getProductById(item.productId);
      if (!product) {
        throw new Error(`Product ${item.productId} not found`);
      }
      if (product.stock < item.quantity) {
        throw new Error(`Insufficient stock for ${product.name} (Requested: ${item.quantity}, Available: ${product.stock})`);
      }
      product.stock -= item.quantity;
      totalAmount += product.price * item.quantity;
      orderItems.push({
        productId: product.id,
        name: product.name,
        quantity: item.quantity,
        unitPrice: product.price,
      });
    }

    const order = {
      id: `ord_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`,
      customerId: customerId || 'anonymous_guest',
      items: orderItems,
      totalAmount: parseFloat(totalAmount.toFixed(2)),
      status: 'CONFIRMED',
      createdAt: new Date().toISOString(),
    };

    this.orders.unshift(order);

    // Emit Realtime Event for WebSockets, SSE & Webhooks
    const event = {
      type: 'ORDER_CREATED',
      topic: 'orders',
      data: order,
    };
    this.emitRealtimeEvent(event);

    return order;
  }

  getOrders(limit = 20) {
    return this.orders.slice(0, limit);
  }

  getOrderById(id) {
    return this.orders.find((o) => o.id === id);
  }

  // Realtime Broadcast Pipeline
  emitRealtimeEvent(event) {
    const eventRecord = {
      id: `evt_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
      ...event,
      timestamp: new Date().toISOString(),
    };

    // Keep bounded history for SSE replay
    this.eventHistory.push(eventRecord);
    if (this.eventHistory.length > this.maxEventHistory) {
      this.eventHistory.shift();
    }

    this.emit('realtime_event', eventRecord);
  }

  // Webhook Delivery Log
  recordWebhookDelivery(delivery) {
    this.webhookDeliveries.unshift({
      id: `wh_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
      ...delivery,
      timestamp: new Date().toISOString(),
    });
    if (this.webhookDeliveries.length > 50) {
      this.webhookDeliveries.pop();
    }
  }

  getWebhookDeliveries() {
    return this.webhookDeliveries;
  }
}

export const store = new DataStore();
