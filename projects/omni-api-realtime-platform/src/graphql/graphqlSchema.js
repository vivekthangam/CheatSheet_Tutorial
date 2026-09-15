import { buildSchema, graphql } from 'graphql';
import { store } from '../store/dataStore.js';

export const schema = buildSchema(`
  type Order {
    id: ID!
    customer: String!
    items: [String!]!
    total: Float!
    status: String!
    priority: String!
    createdAt: String!
  }

  type InventoryItem {
    sku: String!
    name: String!
    quantity: Int!
    unitPrice: Float!
    reorderThreshold: Int!
  }

  type SystemMetric {
    cpuUsagePercent: Float!
    memoryUsageMb: Float!
    activeConnections: Int!
    qps: Int!
    p99LatencyMs: Float!
    timestamp: String!
  }

  type Query {
    orders(status: String, priority: String): [Order!]!
    order(id: ID!): Order
    inventory: [InventoryItem!]!
    systemMetrics: SystemMetric!
  }

  type Mutation {
    createOrder(customer: String!, items: [String!]!, total: Float!, priority: String): Order!
    updateInventory(sku: String!, quantityDelta: Int!): InventoryItem!
  }
`);

export const rootResolver = {
  orders: ({ status, priority }) => {
    let list = store.getOrders();
    if (status) list = list.filter((o) => o.status.toLowerCase() === status.toLowerCase());
    if (priority) list = list.filter((o) => o.priority.toLowerCase() === priority.toLowerCase());
    return list;
  },

  order: ({ id }) => {
    return store.getOrderById(id);
  },

  inventory: () => {
    return store.getInventory();
  },

  systemMetrics: () => {
    return store.getSystemMetrics();
  },

  createOrder: ({ customer, items, total, priority }) => {
    return store.createOrder({ customer, items, total, priority });
  },

  updateInventory: ({ sku, quantityDelta }) => {
    const updated = store.updateInventory(sku, quantityDelta);
    if (!updated) {
      throw new Error(`Inventory item not found for SKU: ${sku}`);
    }
    return updated;
  },
};

/**
 * GraphQL HTTP execution handler
 */
export async function handleGraphQL(req, res) {
  const query = req.body.query || req.query.query;
  const variables = req.body.variables || {};
  const operationName = req.body.operationName;

  if (!query) {
    return res.status(400).json({ errors: [{ message: 'GraphQL query or mutation must be provided' }] });
  }

  try {
    const response = await graphql({
      schema,
      source: query,
      rootValue: rootResolver,
      variableValues: variables,
      operationName,
    });
    return res.status(200).json(response);
  } catch (err) {
    return res.status(500).json({ errors: [{ message: err.message }] });
  }
}
