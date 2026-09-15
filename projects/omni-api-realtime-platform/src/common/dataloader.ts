/**
 * Generic In-Memory DataLoader implementation
 * Batches individual .load(key) calls into a single batch function invocation
 * using the Node.js event loop microtask queue (queueMicrotask or Promise.resolve().then()).
 * Includes per-request in-memory memoization cache.
 */
export type BatchLoadFn<K, V> = (keys: readonly K[]) => Promise<(V | Error)[]>;

export class DataLoader<K, V> {
  private batchFn: BatchLoadFn<K, V>;
  private queue: Array<{
    key: K;
    resolve: (value: V) => void;
    reject: (reason?: any) => void;
  }> = [];
  private cache: Map<K, Promise<V>> = new Map();
  private scheduled = false;

  constructor(batchFn: BatchLoadFn<K, V>) {
    this.batchFn = batchFn;
  }

  public load(key: K): Promise<V> {
    const cached = this.cache.get(key);
    if (cached) {
      return cached;
    }

    const promise = new Promise<V>((resolve, reject) => {
      this.queue.push({ key, resolve, reject });
      if (!this.scheduled) {
        this.scheduled = true;
        queueMicrotask(() => this.dispatchQueue());
      }
    });

    this.cache.set(key, promise);
    return promise;
  }

  public loadMany(keys: readonly K[]): Promise<V[]> {
    return Promise.all(keys.map((k) => this.load(k)));
  }

  public clear(key: K): this {
    this.cache.delete(key);
    return this;
  }

  public clearAll(): this {
    this.cache.clear();
    return this;
  }

  private async dispatchQueue(): Promise<void> {
    const currentQueue = this.queue;
    this.queue = [];
    this.scheduled = false;

    if (currentQueue.length === 0) return;

    const keys = currentQueue.map((item) => item.key);

    try {
      const results = await this.batchFn(keys);
      if (results.length !== keys.length) {
        throw new Error(
          `DataLoader batch function returned ${results.length} items, expected ${keys.length}`
        );
      }

      currentQueue.forEach((item, index) => {
        const result = results[index];
        if (result instanceof Error) {
          item.reject(result);
        } else {
          item.resolve(result);
        }
      });
    } catch (err) {
      currentQueue.forEach((item) => item.reject(err));
    }
  }
}
