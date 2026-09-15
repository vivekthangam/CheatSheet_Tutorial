import { EventEmitter } from 'events';
import { v4 as uuidv4 } from 'uuid';
import { createEnvelope, EventEnvelope } from './envelope';

export interface Product {
  id: string;
  sku: string;
  name: string;
  price: number;
  inventoryCount: number;
}

export interface Customer {
  id: string;
  name: string;
  email: string;
  tier: string;
}

export interface OrderItem {
  productId: string;
  quantity: number;
  unitPrice: number;
}

export interface Order {
  id: string;
  customerId: string;
  items: OrderItem[];
  totalAmount: number;
  currency: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'CANCELLED';
  createdAt: string;
  version: number;
}

export interface IdempotencyRecord {
  status: 'PROCESSING' | 'COMPLETED';
  responseStatus: number;
  responseBody: any;
  createdAt: number;
}

class PlatformStore extends EventEmitter {
  private products: Map<string, Product> = new Map();
  private customers: Map<string, Customer> = new Map();
  private orders: Map<string, Order> = new Map();
  private idempotencyCache: Map<string, IdempotencyRecord> = new Map();
  private eventReplayBuffer: EventEnvelope[] = [];
  private readonly maxReplayBuffer = 500;

  constructor() {
    super();
    this.seedInitialData();
  }

  private seedInitialData() {
    const products: Product[] = [
      { id: 'prod_101', sku: 'LAPTOP-PRO-16', name: 'TitanBook Pro 16"', price: 2499.0, inventoryCount: 45 },
      { id: 'prod_102', sku: 'PHONE-ULTRA-5G', name: 'Apex Ultra 5G Phone', price: 1199.0, inventoryCount: 120 },
      { id: 'prod_103', sku: 'HEADPHONES-ANC', name: 'SonicPulse ANC Headphones', price: 349.0, inventoryCount: 80 },
      { id: 'prod_104', sku: 'SMARTWATCH-SPORT', name: 'Vanguard Chrono Smartwatch', price: 299.0, inventoryCount: 65 },
      { id: 'prod_105', sku: 'DESK-MONITOR-4K', name: 'QuantumView 32" 4K OLED', price: 899.0, inventoryCount: 30 },
    ];
    products.forEach((p) => this.products.set(p.id, p));

    const customers: Customer[] = [
      { id: 'cust_001', name: 'Sarah Connor', email: 'sarah.connor@cyberdyne.io', tier: 'ENTERPRISE_VIP' },
      { id: 'cust_002', name: 'Miles Dyson', email: 'miles.dyson@cyberdyne.io', tier: 'DEVELOPER_PRO' },
      { id: 'cust_003', name: 'John Connor', email: 'john.connor@resistance.net', tier: 'STANDARD' },
    ];
    customers.forEach((c) => this.customers.set(c.id, c));

    // Seed sample order
    const sampleOrder: Order = {
      id: 'ord_initial_1',
      customerId: 'cust_001',
      items: [
        { productId: 'prod_101', quantity: 1, unitPrice: 2499.0 },
        { productId: 'prod_103', quantity: 2, unitPrice: 349.0 },
      ],
      totalAmount: 3197.0,
      currency: 'USD',
      status: 'PROCESSING',
      createdAt: new Date().toISOString(),
      version: 1,
    };
    this.orders.set(sampleOrder.id, sampleOrder);
  }

  // --- Products ---
  public getProducts(): Product[] {
    return Array.from(this.products.values());
  }

  public getProduct(id: string): Product | undefined {
    return this.products.get(id);
  }

  public batchGetProducts(ids: readonly string[]): (Product | Error)[] {
    return ids.map((id) => this.products.get(id) ?? new Error(`Product ${id} not found`));
  }

  // --- Customers ---
  public getCustomers(): Customer[] {
    return Array.from(this.customers.values());
  }

  public getCustomer(id: string): Customer | undefined {
    return this.customers.get(id);
  }

  public batchGetCustomers(ids: readonly string[]): (Customer | Error)[] {
    return ids.map((id) => this.customers.get(id) ?? new Error(`Customer ${id} not found`));
  }

  // --- Orders ---
  public getOrders(options?: {
    first?: number;
    after?: string;
    filterStatus?: string;
  }): {
    orders: Order[];
    hasNextPage: boolean;
    endCursor?: string;
    totalCount: number;
  } {
    let allOrders = Array.from(this.orders.values()).sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );

    if (options?.filterStatus) {
      allOrders = allOrders.filter((o) => o.status === options.filterStatus);
    }

    const totalCount = allOrders.length;
    let startIndex = 0;

    if (options?.after) {
      const decodedCursor = Buffer.from(options.after, 'base64').toString('utf8');
      const foundIdx = allOrders.findIndex((o) => o.id === decodedCursor);
      if (foundIdx !== -1) {
        startIndex = foundIdx + 1;
      }
    }

    const limit = options?.first || 10;
    const paginated = allOrders.slice(startIndex, startIndex + limit);
    const hasNextPage = startIndex + limit < totalCount;
    const endCursor = paginated.length > 0 ? Buffer.from(paginated[paginated.length - 1].id).toString('base64') : undefined;

    return {
      orders: paginated,
      hasNextPage,
      endCursor,
      totalCount,
    };
  }

  public getOrder(id: string): Order | undefined {
    return this.orders.get(id);
  }

  public createOrder(input: {
    customerId: string;
    items: Array<{ productId: string; quantity: number }>;
  }): Order {
    const customer = this.customers.get(input.customerId);
    if (!customer) {
      throw new Error(`Customer ${input.customerId} does not exist`);
    }

    let total = 0;
    const orderItems: OrderItem[] = [];

    for (const item of input.items) {
      const product = this.products.get(item.productId);
      if (!product) {
        throw new Error(`Product ${item.productId} does not exist`);
      }
      if (product.inventoryCount < item.quantity) {
        throw new Error(`Insufficient inventory for product "${product.name}". Available: ${product.inventoryCount}`);
      }
      product.inventoryCount -= item.quantity;
      total += product.price * item.quantity;
      orderItems.push({
        productId: product.id,
        quantity: item.quantity,
        unitPrice: product.price,
      });
    }

    const newOrder: Order = {
      id: `ord_${uuidv4().substring(0, 8)}`,
      customerId: input.customerId,
      items: orderItems,
      totalAmount: Math.round(total * 100) / 100,
      currency: 'USD',
      status: 'PENDING',
      createdAt: new Date().toISOString(),
      version: 1,
    };

    this.orders.set(newOrder.id, newOrder);

    // Emit event in Universal Message Envelope
    const envelope = createEnvelope('order.created', newOrder);
    this.recordAndBroadcast(envelope);

    return newOrder;
  }

  public updateOrderStatus(orderId: string, status: Order['status']): Order {
    const order = this.orders.get(orderId);
    if (!order) {
      throw new Error(`Order ${orderId} not found`);
    }
    order.status = status;
    order.version += 1;

    const envelope = createEnvelope('order.status_changed', order);
    this.recordAndBroadcast(envelope);
    return order;
  }

  // --- Event Replay Buffer ---
  private recordAndBroadcast(envelope: EventEnvelope) {
    this.eventReplayBuffer.push(envelope);
    if (this.eventReplayBuffer.length > this.maxReplayBuffer) {
      this.eventReplayBuffer.shift();
    }
    this.emit('event', envelope);
  }

  public getEventsSince(sequenceNumber: number): EventEnvelope[] {
    return this.eventReplayBuffer.filter((e) => e.header.sequenceNumber > sequenceNumber);
  }

  public getAllRecentEvents(): EventEnvelope[] {
    return [...this.eventReplayBuffer];
  }

  // --- Idempotency ---
  public getIdempotency(key: string): IdempotencyRecord | undefined {
    return this.idempotencyCache.get(key);
  }

  public setIdempotency(key: string, record: IdempotencyRecord) {
    this.idempotencyCache.set(key, record);
  }
}

export const store = new PlatformStore();
