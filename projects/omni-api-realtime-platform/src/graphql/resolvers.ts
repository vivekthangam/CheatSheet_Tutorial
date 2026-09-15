import { store, Order, Product, Customer } from '../common/store';
import { DataLoader } from '../common/dataloader';

export interface GraphQLContext {
  customerLoader: DataLoader<string, Customer>;
  productLoader: DataLoader<string, Product>;
}

export function createGraphQLContext(): GraphQLContext {
  return {
    customerLoader: new DataLoader<string, Customer>(async (keys) => {
      return store.batchGetCustomers(keys);
    }),
    productLoader: new DataLoader<string, Product>(async (keys) => {
      return store.batchGetProducts(keys);
    }),
  };
}

export const resolvers = {
  // --- Custom Scalars ---
  DateTime: {
    serialize: (val: any) => (val instanceof Date ? val.toISOString() : new Date(val).toISOString()),
    parseValue: (val: any) => new Date(val),
  },

  // --- Interfaces ---
  Node: {
    __resolveType(obj: any) {
      if (obj.sku) return 'Product';
      if (obj.tier) return 'Customer';
      if (obj.totalAmount !== undefined) return 'Order';
      return null;
    },
  },

  // --- Queries ---
  Query: {
    node: (_: any, { id }: { id: string }) => {
      const product = store.getProduct(id);
      if (product) return product;
      const customer = store.getCustomer(id);
      if (customer) return customer;
      const order = store.getOrder(id);
      if (order) return order;
      return null;
    },
    product: (_: any, { id }: { id: string }, context: GraphQLContext) => {
      return context.productLoader.load(id);
    },
    products: () => {
      return store.getProducts();
    },
    customer: (_: any, { id }: { id: string }, context: GraphQLContext) => {
      return context.customerLoader.load(id);
    },
    order: (_: any, { id }: { id: string }) => {
      return store.getOrder(id);
    },
    orders: (_: any, { first, after, status }: { first?: number; after?: string; status?: string }) => {
      const { orders, hasNextPage, endCursor, totalCount } = store.getOrders({
        first: first || 10,
        after,
        filterStatus: status,
      });

      const edges = orders.map((o) => ({
        cursor: Buffer.from(o.id).toString('base64'),
        node: o,
      }));

      return {
        edges,
        pageInfo: {
          hasNextPage,
          hasPreviousPage: !!after,
          startCursor: edges.length > 0 ? edges[0].cursor : null,
          endCursor: endCursor || null,
        },
        totalCount,
      };
    },
  },

  // --- Field Resolvers ---
  Order: {
    customer: (parent: Order, _: any, context: GraphQLContext) => {
      // Uses DataLoader to avoid N+1 query waterfall
      return context.customerLoader.load(parent.customerId);
    },
  },

  OrderItem: {
    product: (parent: { productId: string }, _: any, context: GraphQLContext) => {
      // Uses DataLoader to avoid N+1 query waterfall
      return context.productLoader.load(parent.productId);
    },
  },

  // --- Mutations ---
  Mutation: {
    createOrder: (_: any, { input }: { input: { customerId: string; items: any[]; clientMutationId?: string } }) => {
      try {
        const order = store.createOrder({
          customerId: input.customerId,
          items: input.items,
        });
        return {
          order,
          userErrors: [],
          clientMutationId: input.clientMutationId,
        };
      } catch (err: any) {
        return {
          order: null,
          userErrors: [
            {
              field: 'items',
              message: err.message || 'Failed to create order',
              code: 'ORDER_CREATION_FAILED',
            },
          ],
          clientMutationId: input.clientMutationId,
        };
      }
    },
    updateOrderStatus: (_: any, { id, status }: { id: string; status: any }) => {
      return store.updateOrderStatus(id, status);
    },
  },
};
