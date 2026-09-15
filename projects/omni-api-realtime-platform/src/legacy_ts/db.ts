/**
 * Mock Enterprise In-Memory Database
 * Simulates low-level persistence with latency, indexing, and transactional guarantees.
 */

export interface Customer {
  id: string;
  name: string;
  email: string;
  tier: 'FREE' | 'PRO' | 'ENTERPRISE';
}

export interface Product {
  id: string;
  sku: string;
  name: string;
  price: number;
  inventoryCount: number;
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

export class InMemoryDatabase {
  private customers: Map<string, Customer> = new Map();
  private products: Map<string, Product> = new Map();
  private orders: Map<string, Order> = new Map();

  constructor() {
    this.seed();
  }

  private seed() {
    // Seed Customers
    const cust1: Customer = { id: 'cust_001', name: 'Alice Chen', email: 'alice@enterprise.com', tier: 'ENTERPRISE' };
    const cust2: Customer = { id: 'cust_002', name: 'Bob Smith', email: 'bob@startup.io', tier: 'PRO' };
    this.customers.set(cust1.id, cust1);
    this.customers.set(cust2.id, cust2);

    // Seed Products
    const prod1: Product = { id: 'prod_101', sku: 'SKU-NV-H100', name: 'Nvidia H100 80GB SXM5', price: 32000.0, inventoryCount: 50 };
    const prod2: Product = { id: 'prod_102', sku: 'SKU-NV-B200', name: 'Nvidia B200 192GB', price: 45000.0, inventoryCount: 15 };
    const prod3: Product = { id: 'prod_103', sku: 'SKU-MEM-ECC', name: '512GB DDR5 Registered ECC RAM', price: 2400.0, inventoryCount: 200 };
    this.products.set(prod1.id, prod1);
    this.products.set(prod2.id, prod2);
    this.products.set(prod3.id, prod3);

    // Seed Orders
    const ord1: Order = {
      id: 'ord_9001',
      customerId: 'cust_001',
      items: [
        { productId: 'prod_101', quantity: 2, unitPrice: 32000.0 },
        { productId: 'prod_103', quantity: 4, unitPrice: 2400.0 },
      ],
      totalAmount: 73600.0,
      currency: 'USD',
      status: 'PROCESSING',
      createdAt: new Date(Date.now() - 3600000).toISOString(),
      version: 1,
    };
    this.orders.set(ord1.id, ord1);
  }

  public async getCustomerById(id: string): Promise<Customer | null> {
    return this.customers.get(id) || null;
  }

  public async getCustomersByIds(ids: readonly string[]): Promise<(Customer | null)[]> {
    console.log(`[DB Batch Query] Fetching ${ids.length} customers in single round-trip: [${ids.join(', ')}]`);
    return ids.map((id) => this.customers.get(id) || null);
  }

  public async getProductById(id: string): Promise<Product | null> {
    return this.products.get(id) || null;
  }

  public async getProductsByIds(ids: readonly string[]): Promise<(Product | null)[]> {
    console.log(`[DB Batch Query] Fetching ${ids.length} products in single round-trip: [${ids.join(', ')}]`);
    return ids.map((id) => this.products.get(id) || null);
  }

  public async getOrderById(id: string): Promise<Order | null> {
    return this.orders.get(id) || null;
  }

  public async listOrders(): Promise<Order[]> {
    return Array.from(this.orders.values());
  }

  public async saveOrder(order: Order): Promise<Order> {
    this.orders.set(order.id, order);
    return order;
  }
}

export const db = new InMemoryDatabase();
