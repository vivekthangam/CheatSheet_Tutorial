import { buildSchema, graphql } from 'graphql';
import { store } from '../store/dataStore.js';

export const schema = buildSchema(`
  type Product {
    id: ID!
    name: String!
    category: String!
    price: Float!
    stock: Int!
  }

  type OrderItem {
    productId: ID!
    name: String!
    quantity: Int!
    unitPrice: Float!
  }

  type Order {
    id: ID!
    customerId: String!
    totalAmount: Float!
    status: String!
    createdAt: String!
    items: [OrderItem!]!
  }

  type SystemMetrics {
    uptimeSeconds: Float!
    productsCount: Int!
    ordersCount: Int!
  }

  input OrderItemInput {
    productId: ID!
    quantity: Int!
  }

  type Query {
    products(category: String, maxPrice: Float): [Product!]!
    product(id: ID!): Product
    orders(limit: Int): [Order!]!
    order(id: ID!): Order
    metrics: SystemMetrics!
  }

  type Mutation {
    createOrder(customerId: String, items: [OrderItemInput!]!): Order!
    updatePrice(id: ID!, newPrice: Float!): Product
  }
`);

export const rootResolvers = {
  products: ({ category, maxPrice }) => {
    return store.getProducts({ category, maxPrice });
  },
  product: ({ id }) => {
    return store.getProductById(id);
  },
  orders: ({ limit }) => {
    return store.getOrders(limit || 20);
  },
  order: ({ id }) => {
    return store.getOrderById(id);
  },
  metrics: () => {
    return {
      uptimeSeconds: +(process.uptime().toFixed(2)),
      productsCount: store.getProducts().length,
      ordersCount: store.getOrders().length,
    };
  },
  createOrder: ({ customerId, items }) => {
    return store.createOrder({ customerId, items });
  },
  updatePrice: ({ id, newPrice }) => {
    return store.updateProductPrice(id, newPrice);
  },
};

/**
 * GraphQL HTTP Handler for Express
 */
export async function graphqlHandler(req, res) {
  const { query, variables, operationName } = req.body || {};

  if (!query) {
    return res.status(400).json({ errors: [{ message: 'Missing "query" field in request body' }] });
  }

  try {
    const result = await graphql({
      schema,
      source: query,
      rootValue: rootResolvers,
      variableValues: variables,
      operationName,
    });
    return res.json(result);
  } catch (err) {
    return res.status(500).json({ errors: [{ message: err.message }] });
  }
}
