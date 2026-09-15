/**
 * Production DataLoader Context Factory
 * Batches and caches database lookups per-request, mitigating N+1 query explosion.
 */
import DataLoader from 'dataloader';
import { db, Customer, Product } from './db';

export interface AppDataLoaders {
  customerLoader: DataLoader<string, Customer | null>;
  productLoader: DataLoader<string, Product | null>;
}

export function createDataLoaders(): AppDataLoaders {
  return {
    customerLoader: new DataLoader<string, Customer | null>(async (ids: readonly string[]) => {
      return db.getCustomersByIds(ids);
    }),
    productLoader: new DataLoader<string, Product | null>(async (ids: readonly string[]) => {
      return db.getProductsByIds(ids);
    }),
  };
}
