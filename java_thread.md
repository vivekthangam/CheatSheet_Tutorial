[? Back to Home](README.md)

# 🧵 Java Multithreading Complete Tutorial - 100+ Real-World Scenarios
Table of Contents
## 📘 Thread Basics & Lifecycle
## 🔒 Thread Synchronization
## 📘 Advanced Threading (Callable, Future, CompletableFuture)
## 🏊 Thread Pools & Executor Framework
## 📦 Concurrent Collections
## 🔒 Synchronization Tools (CountDownLatch, CyclicBarrier, Semaphore)
## 💀 Deadlock & Race Condition Solutions
## 📡 Thread Communication
## 🌍 Real-World Scenarios

## 🚀 Modern Java Concurrency (Java 21+)

### 🧩 Scenario 26: Virtual Threads (Project Loom)
> **Problem Statement:** Create 100,000 threads to demonstrate the lightweight nature of Virtual Threads compared to Platform Threads.
> **Solution:**
```java
import java.util.concurrent.Executors;
import java.time.Duration;

public class VirtualThreadsDemo {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 100_000; i++) {
                executor.submit(() -> {
                    try {
                        Thread.sleep(Duration.ofMillis(100));
                    } catch (InterruptedException e) {
                        // ignore
                    }
                });
            }
        } // Executor closes and waits for all tasks here automatically
        
        long endTime = System.currentTimeMillis();
        System.out.println("Finished 100k threads in " + (endTime - startTime) + "ms");
    }
}
```
> **Explanation:** Virtual Threads are lightweight threads managed by the JVM, not the OS. You can create millions of them without running out of memory, making blocking I/O operations cheap.

### 🧩 Scenario 27: Structured Concurrency
> **Problem Statement:** Fetch data from two sources (User DB, Order DB) in parallel and fail if either fails, using Structured Concurrency.
> **Solution:**
```java
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class StructuredConcurrencyDemo {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            
            Supplier<String> userTask = scope.fork(() -> {
                Thread.sleep(100);
                return "User: John Doe";
            });
            
            Supplier<String> orderTask = scope.fork(() -> {
                Thread.sleep(200);
                return "Orders: [A, B, C]";
            });
            
            scope.join(); // Wait for both
            scope.throwIfFailed(); // Propagate exception if any failed
            
            System.out.println(userTask.get() + ", " + orderTask.get());
        }
    }
}
```
> **Explanation:** StructuredTasksScope ensures that related concurrent tasks are treated as a single unit of work, simplifying error handling and cancellation.

### 🧩 Scenario 28: StampedLock for Optimistic Reads
> **Problem Statement:** Implement a Point class with high-performance concurrent reads using StampedLock's optimistic locking.
> **Solution:**
```java
import java.util.concurrent.locks.StampedLock;

class Point {
    private double x, y;
    private final StampedLock sl = new StampedLock();

    void move(double deltaX, double deltaY) {
        long stamp = sl.writeLock();
        try {
            x += deltaX;
            y += deltaY;
        } finally {
            sl.unlockWrite(stamp);
        }
    }

    double distanceFromOrigin() {
        long stamp = sl.tryOptimisticRead();
        double currentX = x, currentY = y;
        
        if (!sl.validate(stamp)) { // Check if data changed
            stamp = sl.readLock(); // Fallback to read lock
            try {
                currentX = x;
                currentY = y;
            } finally {
                sl.unlockRead(stamp);
            }
        }
        return Math.sqrt(currentX * currentX + currentY * currentY);
    }
}
```
> **Explanation:** StampedLock allows "optimistic reads" which don't acquire a lock at all unless a modification happens during the read, significantly boosting performance for read-heavy workloads.

### 🧩 Scenario 29: Phaser for Dynamic Barriers
> **Problem Statement:** Coordinate a variable number of parties (threads) that can register and deregister dynamically during execution phases.
> **Solution:**
```java
import java.util.concurrent.Phaser;

public class PhaserDemo {
    public static void main(String[] args) {
        Phaser phaser = new Phaser(1); // Register main thread
        
        for (int i = 0; i < 3; i++) {
            phaser.register(); // Register new party
            new Thread(new Worker(phaser), "Worker-" + i).start();
        }
        
        System.out.println("Phase 0 starting");
        phaser.arriveAndAwaitAdvance(); // Wait for workers
        
        System.out.println("Phase 1 starting");
        phaser.arriveAndAwaitAdvance();
        
        phaser.arriveAndDeregister(); // Main thread done
    }
    
    static class Worker implements Runnable {
        private final Phaser phaser;
        Worker(Phaser phaser) { this.phaser = phaser; }
        
        public void run() {
            System.out.println(Thread.currentThread().getName() + " working phase 0");
            phaser.arriveAndAwaitAdvance();
            
            System.out.println(Thread.currentThread().getName() + " working phase 1");
            phaser.arriveAndAwaitAdvance();
        }
    }
}
```
> **Explanation:** Phaser is a improved CountDownLatch/CyclicBarrier that allows the number of registered parties to change over time.

### 🧩 Scenario 30: Scoped Values (Preview)
> **Problem Statement:** Share data (like a Transaction ID) safely within a request without using ThreadLocal to avoid memory leaks and inheritance issues.
> **Solution:**
```java
import java.util.concurrent.ScopedValue;

public class ScopedValueDemo {
    final static ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

    public static void main(String[] args) {
        ScopedValue.where(REQUEST_ID, "REQ-12345").run(() -> {
            processRequest();
        });
    }

    static void processRequest() {
        System.out.println("Processing " + REQUEST_ID.get());
        // No cleanup needed, automatically scoped!
    }
}
```
> **Explanation:** ScopedValue (Java 21 Preview) allows sharing immutable data for a bounded period of execution, solving many pitfalls of ThreadLocal.

## ✅ Best Practices & Anti-patterns
## 📘 Thread Basics & Lifecycle
### 🧩 Scenario 1: Understanding Thread States
> **Problem Statement:** Create a program that demonstrates all thread states (NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED) with proper state transitions.
> **Solution:**
```java
public class ThreadStatesDemo {
    private static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        // NEW State
        Thread newThread = new Thread(() -> {
            System.out.println("Thread is running");
        });
        System.out.println("NEW State: " + newThread.getState());

        // RUNNABLE State
        newThread.start();
        System.out.println("RUNNABLE State: " + newThread.getState());

        // BLOCKED State
        Thread blockedThread = new Thread(() -> {
            synchronized (lock) {
                System.out.println("Blocked thread acquired lock");
            }
        });

        synchronized (lock) {
            blockedThread.start();
            Thread.sleep(100); // Give blockedThread time to attempt lock
            System.out.println("BLOCKED State: " + blockedThread.getState());
        }

        // WAITING State
        Thread waitingThread = new Thread(() -> {
            try {
                newThread.join(); // Wait for newThread to complete
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        waitingThread.start();
        Thread.sleep(100);
        System.out.println("WAITING State: " + waitingThread.getState());

        // TIMED_WAITING State
        Thread timedWaitingThread = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        timedWaitingThread.start();
        Thread.sleep(100);
        System.out.println("TIMED_WAITING State: " + timedWaitingThread.getState());

        // TERMINATED State
        newThread.join();
        System.out.println("TERMINATED State: " + newThread.getState());
    }
}
```
> **Explanation:** This program demonstrates all six thread states in Java:
NEW: Thread created but not started
RUNNABLE: Thread is executing or ready to execute
BLOCKED: Thread waiting to acquire a monitor lock
WAITING: Thread waiting indefinitely for another thread
TIMED_WAITING: Thread waiting for a specified time
TERMINATED: Thread has completed execution
### 🧩 Scenario 2: Thread Priority and Starvation
> **Problem Statement:** Demonstrate thread priority effects and potential starvation issues when high-priority threads monopolize CPU.
> **Solution:**
```java
public class ThreadPriorityStarvation {
    private static volatile boolean running = true;

    public static void main(String[] args) throws InterruptedException {
        // Low priority thread
        Thread lowPriority = new Thread(() -> {
            int count = 0;
            while (running) {
                count++;
                if (count % 1000000 == 0) {
                    System.out.println("Low priority thread: " + count);
                }
            }
        });

        // High priority thread
        Thread highPriority = new Thread(() -> {
            int count = 0;
            while (running) {
                count++;
                if (count % 1000000 == 0) {
                    System.out.println("High priority thread: " + count);
                }
            }
        });

        lowPriority.setPriority(Thread.MIN_PRIORITY);
        highPriority.setPriority(Thread.MAX_PRIORITY);

        lowPriority.start();
        highPriority.start();

        // Let them run for 5 seconds
        Thread.sleep(5000);
        running = false;

        lowPriority.join();
        highPriority.join();
        System.out.println("Both threads completed");
    }
}
```
> **Explanation:** This demonstrates how thread priorities work in Java. High-priority threads get more CPU time, potentially starving low-priority threads. Note that thread priorities are hints to the OS scheduler and behavior varies across platforms.
### 🧩 Scenario 3: Daemon vs User Threads
> **Problem Statement:** Show the difference between daemon and user threads, demonstrating JVM termination behavior.
> **Solution:**
```java
public class DaemonThreadExample {
    public static void main(String[] args) throws InterruptedException {
        // User thread - JVM waits for completion
        Thread userThread = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println("User thread: " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        // Daemon thread - JVM doesn't wait
        Thread daemonThread = new Thread(() -> {
            while (true) {
                System.out.println("Daemon thread running...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        daemonThread.setDaemon(true);

        userThread.start();
        daemonThread.start();

        System.out.println("Main thread finishing");
        // JVM will exit when userThread completes, daemonThread will be terminated
    }
}
```
> **Explanation:** Daemon threads are background threads that don't prevent JVM termination. When all user threads complete, the JVM exits, terminating any running daemon threads.
## 🔒 Thread Synchronization
### 🧩 Scenario 4: Race Condition in Bank Account
> **Problem Statement:** Simulate a bank account with concurrent deposits and withdrawals, demonstrating race conditions and their solutions.
> **Solution:**
```java
public class BankAccountRaceCondition {
    public static void main(String[] args) throws InterruptedException {
        // Unsafe implementation
        UnsafeBankAccount unsafeAccount = new UnsafeBankAccount(1000);

        Thread depositThread = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                unsafeAccount.deposit(10);
            }
        });

        Thread withdrawThread = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                unsafeAccount.withdraw(10);
            }
        });

        depositThread.start();
        withdrawThread.start();
        depositThread.join();
        withdrawThread.join();

        System.out.println("Unsafe account balance: " + unsafeAccount.getBalance());

        // Safe implementation
        SafeBankAccount safeAccount = new SafeBankAccount(1000);

        Thread safeDepositThread = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                safeAccount.deposit(10);
            }
        });

        Thread safeWithdrawThread = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                safeAccount.withdraw(10);
            }
        });

        safeDepositThread.start();
        safeWithdrawThread.start();
        safeDepositThread.join();
        safeWithdrawThread.join();

        System.out.println("Safe account balance: " + safeAccount.getBalance());
    }
}

class UnsafeBankAccount {
    private int balance;

    public UnsafeBankAccount(int initialBalance) {
        this.balance = initialBalance;
    }

    public void deposit(int amount) {
        balance += amount; // Race condition here
    }

    public void withdraw(int amount) {
        balance -= amount; // Race condition here
    }

    public int getBalance() {
        return balance;
    }
}

class SafeBankAccount {
    private int balance;
    private final Object lock = new Object();

    public SafeBankAccount(int initialBalance) {
        this.balance = initialBalance;
    }

    public void deposit(int amount) {
        synchronized (lock) {
            balance += amount;
        }
    }

    public void withdraw(int amount) {
        synchronized (lock) {
            balance -= amount;
        }
    }

    public int getBalance() {
        synchronized (lock) {
            return balance;
        }
    }
}
```
> **Explanation:** The unsafe implementation shows race conditions where multiple threads modify shared data simultaneously. The synchronized implementation ensures thread safety by using locks.
### 🧩 Scenario 5: Synchronized vs ReentrantLock
> **Problem Statement:** Compare synchronized keyword with ReentrantLock, showing advanced features of ReentrantLock.
> **Solution:**
```java
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public class SynchronizedVsReentrantLock {
    private int synchronizedCounter = 0;
    private int reentrantLockCounter = 0;
    private final ReentrantLock lock = new ReentrantLock();

    // Synchronized method
    public synchronized void synchronizedIncrement() {
        synchronizedCounter++;
    }

    // ReentrantLock method with try-finally
    public void reentrantLockIncrement() {
        lock.lock();
        try {
            reentrantLockCounter++;
        } finally {
            lock.unlock();
        }
    }

    // Advanced ReentrantLock features
    public boolean tryLockExample() {
        try {
            if (lock.tryLock(1, TimeUnit.SECONDS)) {
                try {
                    // Critical section
                    Thread.sleep(100);
                    return true;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    public static void main(String[] args) throws InterruptedException {
        SynchronizedVsReentrantLock example = new SynchronizedVsReentrantLock();

        // Test both approaches
        Thread[] threads = new Thread[10];

        for (int i = 0; i < 5; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    example.synchronizedIncrement();
                }
            });
        }

        for (int i = 5; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    example.reentrantLockIncrement();
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Synchronized counter: " + example.synchronizedCounter);
        System.out.println("ReentrantLock counter: " + example.reentrantLockCounter);

        // Test tryLock
        Thread tryLockThread = new Thread(() -> {
            boolean acquired = example.tryLockExample();
            System.out.println("TryLock acquired: " + acquired);
        });
        tryLockThread.start();
        tryLockThread.join();
    }
}
```
> **Explanation:** ReentrantLock provides more advanced features than synchronized:
tryLock(): Attempts to acquire lock with timeout
lockInterruptibly(): Allows interruption while waiting for lock
Fair locking: Threads get lock in FIFO order
Multiple conditions: Can create multiple condition variables
### 🧩 Scenario 6: ReadWriteLock for Database Cache
> **Problem Statement:** Implement a thread-safe database cache that allows multiple concurrent reads but exclusive writes.
> **Solution:**
```java
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.HashMap;
import java.util.Map;

public class DatabaseCache {
    private final Map<String, String> cache = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    // Multiple threads can read simultaneously
    public String get(String key) {
        rwLock.readLock().lock();
        try {
            // Simulate database read delay
            Thread.sleep(10);
            return cache.get(key);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // Only one thread can write at a time
    public void put(String key, String value) {
        rwLock.writeLock().lock();
        try {
            // Simulate database write delay
            Thread.sleep(50);
            cache.put(key, value);
            System.out.println("Cached: " + key + " = " + value);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        DatabaseCache cache = new DatabaseCache();

        // Pre-populate cache
        cache.put("user:1", "John Doe");
        cache.put("user:2", "Jane Smith");

        // Multiple readers
        Thread[] readers = new Thread[5];
        for (int i = 0; i < readers.length; i++) {
            final int readerId = i;
            readers[i] = new Thread(() -> {
                for (int j = 0; j < 3; j++) {
                    String value = cache.get("user:" + (readerId % 2 + 1));
                    System.out.println("Reader " + readerId + " read: " + value);
                }
            });
        }

        // Single writer
        Thread writer = new Thread(() -> {
            for (int i = 3; i < 6; i++) {
                cache.put("user:" + i, "User " + i);
            }
        });

        // Start all threads
        for (Thread reader : readers) {
            reader.start();
        }
        writer.start();

        // Wait for completion
        for (Thread reader : readers) {
            reader.join();
        }
        writer.join();

        System.out.println("All operations completed");
    }
}
```
> **Explanation:** ReadWriteLock allows multiple concurrent reads (improving performance) while ensuring exclusive access for writes (maintaining data integrity).
## 📘 Advanced Threading (Callable, Future, CompletableFuture)
### 🧩 Scenario 7: Callable vs Runnable Comparison
> **Problem Statement:** Compare Callable and Runnable interfaces, showing how to handle return values and exceptions.
> **Solution:**
```java
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ExecutionException;

public class CallableVsRunnable {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // Runnable example - no return value
        Runnable runnableTask = () -> {
            System.out.println("Runnable task executing");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("Runnable task completed");
        };

        Thread runnableThread = new Thread(runnableTask);
        runnableThread.start();
        runnableThread.join();

        // Callable example - returns value and throws exception
        Callable<String> callableTask = () -> {
            System.out.println("Callable task executing");
            Thread.sleep(1000);
            if (Math.random() < 0.5) {
                throw new Exception("Simulated task failure");
            }
            return "Task completed successfully";
        };

        FutureTask<String> futureTask = new FutureTask<>(callableTask);
        Thread callableThread = new Thread(futureTask);
        callableThread.start();

        try {
            String result = futureTask.get(); // Blocks until result is available
            System.out.println("Callable result: " + result);
        } catch (ExecutionException e) {
            System.out.println("Callable threw exception: " + e.getCause().getMessage());
        }

        callableThread.join();
    }
}
```
> **Explanation:** Callable is more powerful than Runnable:
Return values: Can return results
Exception handling: Can throw checked exceptions
Future integration: Works with FutureTask and Executor framework
### 🧩 Scenario 8: Multiple Callable Tasks with ExecutorService
> **Problem Statement:** Process multiple independent tasks concurrently using Callable and collect all results.
> **Solution:**
```java
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class MultipleCallableTasks {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(5);

        List<Callable<String>> tasks = new ArrayList<>();

        // Create multiple tasks
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            tasks.add(() -> {
                // Simulate different task durations
                Thread.sleep((long) (Math.random() * 2000));
                return "Task " + taskId + " completed by " + Thread.currentThread().getName();
            });
        }

        // Execute all tasks
        List<Future<String>> futures = executor.invokeAll(tasks);

        // Process results as they complete
        for (int i = 0; i < futures.size(); i++) {
            Future<String> future = futures.get(i);
            try {
                String result = future.get(); // This won't block as invokeAll already waited
                System.out.println("Result: " + result);
            } catch (ExecutionException e) {
                System.out.println("Task " + i + " failed: " + e.getCause().getMessage());
            }
        }

        // Alternative: Get any completed task
        String anyResult = executor.invokeAny(tasks);
        System.out.println("First completed result: " + anyResult);

        executor.shutdown();
    }
}
```
> **Explanation:** invokeAll() executes all tasks and returns when all complete, while invokeAny() returns the first successful result and cancels remaining tasks.
### 🧩 Scenario 9: CompletableFuture for Async Operations
> **Problem Statement:** Implement an e-commerce order processing system using CompletableFuture to handle inventory check, payment processing, and shipping asynchronously.
> **Solution:**
```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class EcommerceOrderProcessing {

    // Simulate inventory service
    public static CompletableFuture<Boolean> checkInventory(String productId, int quantity) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(500); // Simulate network delay
                return quantity <= 100; // Assume we have 100 items in stock
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        });
    }

    // Simulate payment service
    public static CompletableFuture<Boolean> processPayment(String paymentMethod, double amount) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000); // Simulate payment processing
                return !paymentMethod.equals("invalid"); // Simulate payment failure
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        });
    }

    // Simulate shipping service
    public static CompletableFuture<String> scheduleShipping(String address) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(800); // Simulate shipping calculation
                return "TRACK-" + System.currentTimeMillis();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "ERROR";
            }
        });
    }

    // Simulate notification service
    public static CompletableFuture<Void> sendNotification(String email, String message) {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(200); // Simulate email sending
                System.out.println("Email sent to " + email + ": " + message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        String productId = "PROD-123";
        int quantity = 5;
        String paymentMethod = "credit_card";
        double amount = 299.99;
        String customerEmail = "customer@example.com";
        String shippingAddress = "123 Main St, City, Country";

        // Process order with all services running in parallel
        CompletableFuture<Boolean> inventoryFuture = checkInventory(productId, quantity);
        CompletableFuture<Boolean> paymentFuture = processPayment(paymentMethod, amount);

        // Wait for both inventory and payment to complete
        CompletableFuture<Boolean> orderValidation = inventoryFuture
            .thenCombine(paymentFuture, (inventoryResult, paymentResult) ->
                inventoryResult && paymentResult);

        // If order is valid, proceed with shipping
        CompletableFuture<String> shippingFuture = orderValidation
            .thenCompose(valid -> {
                if (valid) {
                    return scheduleShipping(shippingAddress);
                } else {
                    return CompletableFuture.completedFuture("FAILED");
                }
            });

        // Send notification regardless of result
        CompletableFuture<Void> notificationFuture = shippingFuture
            .thenCompose(trackingNumber -> {
                String message;
                if ("FAILED".equals(trackingNumber)) {
                    message = "Order failed - please check inventory or payment method";
                } else {
                    message = "Order successful! Tracking number: " + trackingNumber;
                }
                return sendNotification(customerEmail, message);
            });

        // Handle exceptions
        CompletableFuture<String> finalResult = shippingFuture
            .handle((result, throwable) -> {
                if (throwable != null) {
                    System.out.println("Error processing order: " + throwable.getMessage());
                    return "ERROR";
                }
                return result;
            });

        // Wait for everything to complete
        String result = finalResult.get();
        notificationFuture.get(); // Wait for notification

        System.out.println("Order processing result: " + result);

        // Alternative: Timeout handling
        CompletableFuture<String> timeoutFuture = shippingFuture
            .completeOnTimeout("TIMEOUT", 2, java.util.concurrent.TimeUnit.SECONDS);

        System.out.println("Result with timeout: " + timeoutFuture.get());
    }
}
```
> **Explanation:** CompletableFuture provides:
Chaining: thenCompose() for sequential operations
Combining: thenCombine() for parallel operations
Error handling: handle() and exceptionally()
Timeouts: completeOnTimeout() and orTimeout()
Async execution: All operations run in separate threads
### 🧩 Scenario 10: CompletableFuture Exception Handling
> **Problem Statement:** Build a robust API client with retry logic, circuit breaker pattern, and fallback mechanisms using CompletableFuture.
> **Solution:**
```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class RobustApiClient {
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;

    // Simulate unreliable API
    public static CompletableFuture<String> callUnreliableApi(String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            // Simulate 50% failure rate
            if (ThreadLocalRandom.current().nextDouble() < 0.5) {
                throw new RuntimeException("API call failed for endpoint: " + endpoint);
            }
            return "Success: " + endpoint;
        });
    }

    // Retry mechanism with exponential backoff
    public static CompletableFuture<String> retryWithBackoff(
            String endpoint, int maxRetries, long initialDelay) {

        return callUnreliableApi(endpoint)
            .handle((result, throwable) -> {
                if (throwable == null) {
                    return CompletableFuture.completedFuture(result);
                } else if (maxRetries > 0) {
                    System.out.println("Retrying " + endpoint + ", attempts left: " + maxRetries);
                    return CompletableFuture
                        .delayedExecutor(initialDelay, TimeUnit.MILLISECONDS)
                        .execute(() -> retryWithBackoff(endpoint, maxRetries - 1, initialDelay * 2));
                } else {
                    return CompletableFuture.<String>failedFuture(throwable);
                }
            })
            .thenCompose(cf -> cf);
    }

    // Circuit breaker pattern
    static class CircuitBreaker {
        private final int failureThreshold;
        private final long timeout;
        private int failureCount = 0;
        private long lastFailureTime = 0;
        private State state = State.CLOSED;

        enum State { CLOSED, OPEN, HALF_OPEN }

        public CircuitBreaker(int failureThreshold, long timeout) {
            this.failureThreshold = failureThreshold;
            this.timeout = timeout;
        }

        public <T> CompletableFuture<T> execute(Supplier<CompletableFuture<T>> supplier) {
            if (state == State.OPEN) {
                if (System.currentTimeMillis() - lastFailureTime > timeout) {
                    state = State.HALF_OPEN;
                } else {
                    return CompletableFuture.failedFuture(
                        new RuntimeException("Circuit breaker is OPEN"));
                }
            }

            return supplier.get()
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        onFailure();
                    } else {
                        onSuccess();
                    }
                });
        }

        private synchronized void onFailure() {
            failureCount++;
            lastFailureTime = System.currentTimeMillis();
            if (failureCount >= failureThreshold) {
                state = State.OPEN;
            }
        }

        private synchronized void onSuccess() {
            failureCount = 0;
            state = State.CLOSED;
        }
    }

    // Fallback mechanism
    public static CompletableFuture<String> withFallback(String endpoint) {
        return callUnreliableApi(endpoint)
            .exceptionally(throwable -> {
                System.out.println("Primary API failed, using fallback: " + throwable.getMessage());
                return callFallbackApi(endpoint);
            })
            .thenCompose(result -> {
                if (result.startsWith("Fallback")) {
                    return CompletableFuture.completedFuture(result);
                } else {
                    return CompletableFuture.completedFuture(result);
                }
            });
    }

    private static String callFallbackApi(String endpoint) {
        // Simulate fallback API call
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Fallback result for: " + endpoint;
    }

    public static void main(String[] args) throws Exception {
        // Test retry mechanism
        System.out.println("=== Testing Retry Mechanism ===");
        CompletableFuture<String> retryResult = retryWithBackoff("/api/users", MAX_RETRIES, RETRY_DELAY_MS);
        System.out.println("Retry result: " + retryResult.get());

        // Test circuit breaker
        System.out.println("\n=== Testing Circuit Breaker ===");
        CircuitBreaker cb = new CircuitBreaker(3, 2000);

        // Simulate multiple failures
        for (int i = 0; i < 5; i++) {
            final int attempt = i;
            cb.execute(() -> callUnreliableApi("/api/data" + attempt))
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        System.out.println("Attempt " + attempt + " failed: " + throwable.getMessage());
                    } else {
                        System.out.println("Attempt " + attempt + " succeeded: " + result);
                    }
                    return null;
                })
                .get();

            Thread.sleep(500); // Small delay between attempts
        }

        // Test fallback mechanism
        System.out.println("\n=== Testing Fallback Mechanism ===");
        CompletableFuture<String> fallbackResult = withFallback("/api/orders");
        System.out.println("Fallback result: " + fallbackResult.get());

        // Combine all patterns
        System.out.println("\n=== Combined Robust Pattern ===");
        CircuitBreaker combinedCB = new CircuitBreaker(2, 1000);

        CompletableFuture<String> robustResult = combinedCB.execute(() ->
            retryWithBackoff("/api/critical", 2, 500)
                .exceptionally(throwable -> "Critical fallback due to: " + throwable.getMessage())
        );

        System.out.println("Robust result: " + robustResult.get());
    }
}
```
> **Explanation:** This demonstrates advanced error handling patterns:
Retry with exponential backoff: Automatically retries failed operations
Circuit breaker: Prevents cascading failures by failing fast when error threshold is reached
Fallback mechanisms: Provides alternative responses when primary service fails
Exceptionally: Handles exceptions and provides fallback values
## 🏊 Thread Pools & Executor Framework
### 🧩 Scenario 11: Fixed vs Cached Thread Pool
> **Problem Statement:** Compare different types of thread pools (Fixed, Cached, Single, Scheduled) and their appropriate use cases.
> **Solution:**
```java
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

public class ThreadPoolComparison {

    public static void demonstrateFixedThreadPool() throws InterruptedException {
        System.out.println("=== Fixed Thread Pool (3 threads) ===");
        ExecutorService fixedPool = Executors.newFixedThreadPool(3);

        // Submit 10 tasks to 3 threads
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            fixedPool.submit(() -> {
                System.out.println("Task " + taskId + " running on " + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        fixedPool.shutdown();
        fixedPool.awaitTermination(15, TimeUnit.SECONDS);
    }

    public static void demonstrateCachedThreadPool() throws InterruptedException {
        System.out.println("\n=== Cached Thread Pool (unbounded) ===");
        ExecutorService cachedPool = Executors.newCachedThreadPool();

        // Submit 10 tasks - will create new threads as needed
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            cachedPool.submit(() -> {
                System.out.println("Task " + taskId + " running on " + Thread.currentThread().getName());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        cachedPool.shutdown();
        cachedPool.awaitTermination(10, TimeUnit.SECONDS);
    }

    public static void demonstrateSingleThreadExecutor() throws InterruptedException {
        System.out.println("\n=== Single Thread Executor ===");
        ExecutorService singleThread = Executors.newSingleThreadExecutor();

        // Submit 5 tasks - will execute sequentially
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            singleThread.submit(() -> {
                System.out.println("Task " + taskId + " running on " + Thread.currentThread().getName());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        singleThread.shutdown();
        singleThread.awaitTermination(10, TimeUnit.SECONDS);
    }

    public static void demonstrateScheduledThreadPool() throws InterruptedException {
        System.out.println("\n=== Scheduled Thread Pool ===");
        ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(2);

        // Schedule one-time task with delay
        scheduledPool.schedule(() -> {
            System.out.println("One-time task executed after 2 seconds");
        }, 2, TimeUnit.SECONDS);

        // Schedule periodic task
        ScheduledFuture<?> periodicTask = scheduledPool.scheduleAtFixedRate(() -> {
            System.out.println("Periodic task executed at " + System.currentTimeMillis());
        }, 1, 3, TimeUnit.SECONDS);

        // Schedule task with fixed delay
        scheduledPool.scheduleWithFixedDelay(() -> {
            System.out.println("Fixed delay task executed at " + System.currentTimeMillis());
            try {
                Thread.sleep(1000); // Simulate task duration
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 0, 2, TimeUnit.SECONDS);

        // Let tasks run for 10 seconds
        Thread.sleep(10000);

        // Cancel periodic tasks
        periodicTask.cancel(false);
        scheduledPool.shutdown();
    }

    public static void demonstrateCustomThreadPool() throws InterruptedException {
        System.out.println("\n=== Custom Thread Pool with Rejection Policy ===");

        // Create custom thread pool
        ThreadPoolExecutor customPool = new ThreadPoolExecutor(
            2, // core pool size
            4, // maximum pool size
            60, // keep alive time
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(2), // work queue
            new ThreadPoolExecutor.CallerRunsPolicy() // rejection policy
        );

        // Submit 10 tasks
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            customPool.submit(() -> {
                System.out.println("Task " + taskId + " running on " + Thread.currentThread().getName());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        // Monitor pool status
        while (!customPool.isTerminated()) {
            System.out.println("Active: " + customPool.getActiveCount() +
                             ", Completed: " + customPool.getCompletedTaskCount() +
                             ", Queue: " + customPool.getQueue().size());
            Thread.sleep(1000);
        }

        customPool.shutdown();
        customPool.awaitTermination(30, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws InterruptedException {
        demonstrateFixedThreadPool();
        demonstrateCachedThreadPool();
        demonstrateSingleThreadExecutor();
        demonstrateScheduledThreadPool();
        demonstrateCustomThreadPool();
    }
}
```
> **Explanation:** Different thread pool types serve different purposes:
FixedThreadPool: Limited number of threads, good for CPU-intensive tasks
CachedThreadPool: Unbounded threads, good for short-lived tasks
SingleThreadExecutor: Sequential execution, good for tasks requiring order
ScheduledThreadPool: For delayed and periodic tasks
Custom ThreadPool: Full control over parameters and rejection policies
### 🧩 Scenario 12: ForkJoinPool for Recursive Tasks
> **Problem Statement:** Implement parallel merge sort using ForkJoinPool to demonstrate divide-and-conquer algorithms.
> **Solution:**
```java
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;
import java.util.Arrays;

public class ParallelMergeSort {

    static class MergeSortTask extends RecursiveAction {
        private static final int THRESHOLD = 1000; // Sequential threshold
        private final int[] array;
        private final int start;
        private final int end;

        public MergeSortTask(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected void compute() {
            if (end - start <= THRESHOLD) {
                // Sequential sort for small arrays
                Arrays.sort(array, start, end);
            } else {
                int mid = start + (end - start) / 2;

                // Create subtasks
                MergeSortTask leftTask = new MergeSortTask(array, start, mid);
                MergeSortTask rightTask = new MergeSortTask(array, mid, end);

                // Fork both tasks
                leftTask.fork();
                rightTask.fork();

                // Wait for both to complete
                leftTask.join();
                rightTask.join();

                // Merge results
                merge(array, start, mid, end);
            }
        }

        private void merge(int[] array, int start, int mid, int end) {
            int[] temp = new int[end - start];
            int i = start, j = mid, k = 0;

            while (i < mid && j < end) {
                if (array[i] <= array[j]) {
                    temp[k++] = array[i++];
                } else {
                    temp[k++] = array[j++];
                }
            }

            while (i < mid) {
                temp[k++] = array[i++];
            }

            while (j < end) {
                temp[k++] = array[j++];
            }

            System.arraycopy(temp, 0, array, start, temp.length);
        }
    }

    // Task with return value
    static class SumTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 10000;
        private final int[] array;
        private final int start;
        private final int end;

        public SumTask(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            if (end - start <= THRESHOLD) {
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum += array[i];
                }
                return sum;
            } else {
                int mid = start + (end - start) / 2;

                SumTask leftTask = new SumTask(array, start, mid);
                SumTask rightTask = new SumTask(array, mid, end);

                leftTask.fork();
                long rightResult = rightTask.compute(); // Work stealing
                long leftResult = leftTask.join();

                return leftResult + rightResult;
            }
        }
    }

    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool();

        // Test merge sort
        int[] array = new int[100000];
        for (int i = 0; i < array.length; i++) {
            array[i] = (int) (Math.random() * 1000000);
        }

        long startTime = System.currentTimeMillis();
        MergeSortTask sortTask = new MergeSortTask(array, 0, array.length);
        pool.invoke(sortTask);
        long endTime = System.currentTimeMillis();

        System.out.println("Parallel merge sort completed in " + (endTime - startTime) + " ms");
        System.out.println("First 10 elements: " + Arrays.toString(Arrays.copyOf(array, 10)));

        // Test parallel sum
        int[] numbers = new int[10000000];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = i + 1;
        }

        startTime = System.currentTimeMillis();
        SumTask sumTask = new SumTask(numbers, 0, numbers.length);
        long sum = pool.invoke(sumTask);
        endTime = System.currentTimeMillis();

        System.out.println("Parallel sum completed in " + (endTime - startTime) + " ms");
        System.out.println("Sum of 1 to " + numbers.length + " = " + sum);

        // Compare with sequential sum
        startTime = System.currentTimeMillis();
        long sequentialSum = 0;
        for (int number : numbers) {
            sequentialSum += number;
        }
        endTime = System.currentTimeMillis();

        System.out.println("Sequential sum completed in " + (endTime - startTime) + " ms");
        System.out.println("Sequential sum = " + sequentialSum);

        pool.shutdown();
    }
}
```
> **Explanation:** ForkJoinPool uses work-stealing algorithm:
RecursiveTask: Returns a result
RecursiveAction: No return value
fork(): Asynchronously executes task
join(): Waits for result
Work stealing: Idle threads steal tasks from busy threads' queues
### 🧩 Scenario 13: ExecutorService with CompletionService
> **Problem Statement:** Process multiple image download tasks and handle results as they complete, not in submission order.
> **Solution:**
```java
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

public class ImageDownloadProcessor {

    static class ImageDownloadTask implements Callable<String> {
        private final String imageUrl;
        private final int downloadTime;

        public ImageDownloadTask(String imageUrl, int downloadTime) {
            this.imageUrl = imageUrl;
            this.downloadTime = downloadTime;
        }

        @Override
        public String call() throws Exception {
            System.out.println("Starting download: " + imageUrl);
            Thread.sleep(downloadTime); // Simulate download time

            // Simulate random failures
            if (Math.random() < 0.2) {
                throw new Exception("Network error downloading " + imageUrl);
            }

            String result = "Downloaded: " + imageUrl + " (size: " + (100 + Math.random() * 900) + "KB)";
            System.out.println("Completed: " + imageUrl);
            return result;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CompletionService<String> completionService = new ExecutorCompletionService<>(executor);

        // Submit multiple image download tasks
        List<String> imageUrls = List.of(
            "https://example.com/image1.jpg",
            "https://example.com/image2.jpg",
            "https://example.com/image3.jpg",
            "https://example.com/image4.jpg",
            "https://example.com/image5.jpg",
            "https://example.com/image6.jpg"
        );

        // Submit tasks with different download times
        for (int i = 0; i < imageUrls.size(); i++) {
            int downloadTime = 1000 + (i * 500); // Varying download times
            completionService.submit(new ImageDownloadTask(imageUrls.get(i), downloadTime));
        }

        // Process results as they complete
        int completed = 0;
        int failed = 0;

        while (completed + failed < imageUrls.size()) {
            try {
                Future<String> result = completionService.poll(5, TimeUnit.SECONDS);
                if (result != null) {
                    try {
                        String imageData = result.get();
                        System.out.println("Processed result: " + imageData);
                        completed++;
                    } catch (ExecutionException e) {
                        System.out.println("Download failed: " + e.getCause().getMessage());
                        failed++;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("Download summary: " + completed + " successful, " + failed + " failed");

        // Alternative: Process with timeout per task
        System.out.println("\n=== Processing with timeouts ===");
        CompletionService<String> timeoutService = new ExecutorCompletionService<>(executor);

        // Resubmit tasks
        for (int i = 0; i < imageUrls.size(); i++) {
            int downloadTime = 1000 + (i * 300);
            timeoutService.submit(new ImageDownloadTask(imageUrls.get(i), downloadTime));
        }

        // Process with individual timeouts
        for (int i = 0; i < imageUrls.size(); i++) {
            try {
                Future<String> future = timeoutService.poll(2, TimeUnit.SECONDS);
                if (future != null) {
                    String result = future.get(1, TimeUnit.SECONDS);
                    System.out.println("Got result: " + result);
                } else {
                    System.out.println("Task " + i + " timed out");
                }
            } catch (TimeoutException e) {
                System.out.println("Task " + i + " took too long");
            } catch (Exception e) {
                System.out.println("Task " + i + " failed: " + e.getMessage());
            }
        }

        executor.shutdown();
    }
}
```
> **Explanation:** CompletionService provides:
Non-blocking result retrieval: poll() returns completed tasks immediately
Order by completion: Results available as soon as tasks finish
Timeout handling: Can specify timeouts for individual tasks
Better resource utilization: Process results while other tasks still running
## 🔒 Synchronization Tools (CountDownLatch, CyclicBarrier, Semaphore)
### 🧩 Scenario 14: CountDownLatch for Service Startup
> **Problem Statement:** Implement a microservices startup sequence where the main application waits for all services to initialize before proceeding.
> **Solution:**
```java
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MicroservicesStartup {

    static class Service implements Runnable {
        private final String serviceName;
        private final int startupTime;
        private final CountDownLatch latch;
        private volatile boolean started = false;

        public Service(String serviceName, int startupTime, CountDownLatch latch) {
            this.serviceName = serviceName;
            this.startupTime = startupTime;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                System.out.println(serviceName + " is starting...");
                Thread.sleep(startupTime); // Simulate startup time

                // Simulate potential startup failure
                if (Math.random() < 0.1) {
                    throw new Exception(serviceName + " failed to start");
                }

                started = true;
                System.out.println(serviceName + " started successfully");

            } catch (Exception e) {
                System.err.println("Error starting " + serviceName + ": " + e.getMessage());
            } finally {
                latch.countDown(); // Always count down
                System.out.println(serviceName + " countdown, remaining: " + latch.getCount());
            }
        }

        public boolean isStarted() {
            return started;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int serviceCount = 5;
        CountDownLatch startupLatch = new CountDownLatch(serviceCount);

        // Create and start services
        Service[] services = {
            new Service("Database Service", 2000, startupLatch),
            new Service("Cache Service", 1000, startupLatch),
            new Service("Authentication Service", 1500, startupLatch),
            new Service("API Gateway", 1200, startupLatch),
            new Service("Message Queue", 1800, startupLatch)
        };

        // Start all services concurrently
        for (Service service : services) {
            new Thread(service).start();
        }

        // Wait for all services to start (with timeout)
        System.out.println("Waiting for all services to start...");
        boolean allStarted = startupLatch.await(10, TimeUnit.SECONDS);

        if (allStarted) {
            System.out.println("All services started successfully!");

            // Verify each service status
            int successfullyStarted = 0;
            for (Service service : services) {
                if (service.isStarted()) {
                    successfullyStarted++;
                }
            }
            System.out.println("Successfully started services: " + successfullyStarted + "/" + serviceCount);

            // Start application
            startApplication();
        } else {
            System.err.println("Timeout waiting for services to start. Proceeding with degraded mode.");
            long remaining = startupLatch.getCount();
            System.err.println("Services that didn't start: " + remaining);
        }
    }

    private static void startApplication() {
        System.out.println("Starting main application...");
        // Application startup logic here
        System.out.println("Application started successfully!");
    }
}
```
> **Explanation:** CountDownLatch is a one-time synchronization aid:
countDown(): Decrements the count
await(): Blocks until count reaches zero
One-time use: Cannot be reset after count reaches zero
Multiple waiters: Multiple threads can await the same latch
### 🧩 Scenario 15: CyclicBarrier for Multiplayer Game
> **Problem Statement:** Implement a multiplayer game lobby where all players must ready up before the game starts, and the game can be played multiple rounds.
> **Solution:**
```java
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

public class MultiplayerGameLobby {

    static class Player implements Runnable {
        private final String playerName;
        private final CyclicBarrier readyBarrier;
        private final CyclicBarrier roundBarrier;
        private volatile boolean isReady = false;

        public Player(String playerName, CyclicBarrier readyBarrier, CyclicBarrier roundBarrier) {
            this.playerName = playerName;
            this.readyBarrier = readyBarrier;
            this.roundBarrier = roundBarrier;
        }

        @Override
        public void run() {
            try {
                for (int round = 1; round <= 3; round++) {
                    System.out.println(playerName + " joining round " + round);

                    // Player takes time to get ready
                    Thread.sleep((long) (Math.random() * 3000));

                    isReady = true;
                    System.out.println(playerName + " is ready for round " + round);

                    // Wait for all players to be ready
                    try {
                        readyBarrier.await();
                        System.out.println(playerName + " starting round " + round);

                        // Play the round
                        playRound(round);

                        // Wait for all players to finish the round
                        roundBarrier.await();

                    } catch (BrokenBarrierException e) {
                        System.err.println(playerName + " detected broken barrier");
                        break;
                    }
                }

                System.out.println(playerName + " finished all rounds");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println(playerName + " was interrupted");
            }
        }

        private void playRound(int round) throws InterruptedException {
            // Simulate playing the round
            System.out.println(playerName + " is playing round " + round);
            Thread.sleep((long) (Math.random() * 2000 + 1000));

            int score = (int) (Math.random() * 100);
            System.out.println(playerName + " scored " + score + " in round " + round);
        }
    }

    static class GameCoordinator implements Runnable {
        private final CyclicBarrier barrier;

        public GameCoordinator(CyclicBarrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public void run() {
            System.out.println("Game Coordinator: All players ready! Starting game...");
            // Additional game setup logic here
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int playerCount = 4;

        // Create cyclic barriers
        CyclicBarrier readyBarrier = new CyclicBarrier(playerCount,
            () -> System.out.println("=== ALL PLAYERS READY - GAME STARTING ==="));

        CyclicBarrier roundEndBarrier = new CyclicBarrier(playerCount,
            () -> System.out.println("=== ROUND COMPLETED ==="));

        // Create and start players
        Player[] players = new Player[playerCount];
        Thread[] playerThreads = new Thread[playerCount];

        for (int i = 0; i < playerCount; i++) {
            players[i] = new Player("Player" + (i + 1), readyBarrier, roundEndBarrier);
            playerThreads[i] = new Thread(players[i]);
            playerThreads[i].start();
        }

        // Wait for all players to complete
        for (Thread thread : playerThreads) {
            thread.join();
        }

        System.out.println("Game session completed!");

        // Demonstrate barrier reset
        System.out.println("\n=== Starting new game session ===");

        // Barriers are automatically reset, can be reused
        for (int i = 0; i < playerCount; i++) {
            players[i] = new Player("NewPlayer" + (i + 1), readyBarrier, roundEndBarrier);
            playerThreads[i] = new Thread(players[i]);
            playerThreads[i].start();
        }

        for (Thread thread : playerThreads) {
            thread.join();
        }

        System.out.println("New game session completed!");
    }
}
```
> **Explanation:** CyclicBarrier is reusable and supports:
Reset capability: Can be used multiple times
Barrier action: Runnable executed when all threads reach barrier
Broken barrier: Detects when threads leave due to interruption/timeout
await(): Threads wait until all reach the barrier
### 🧩 Scenario 16: Semaphore for Resource Pool
> **Problem Statement:** Implement a database connection pool using Semaphore to limit concurrent connections.
> **Solution:**
```java
import java.util.concurrent.Semaphore;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class DatabaseConnectionPool {

    static class DatabaseConnection {
        private final String connectionId;
        private volatile boolean inUse = false;

        public DatabaseConnection(String connectionId) {
            this.connectionId = connectionId;
        }

        public void executeQuery(String query) throws InterruptedException {
            System.out.println("Executing query on " + connectionId + ": " + query);
            Thread.sleep(1000); // Simulate query execution
            System.out.println("Query completed on " + connectionId);
        }

        public void close() {
            inUse = false;
            System.out.println("Connection " + connectionId + " returned to pool");
        }

        public String getConnectionId() {
            return connectionId;
        }

        public boolean isInUse() {
            return inUse;
        }

        public void setInUse(boolean inUse) {
            this.inUse = inUse;
        }
    }

    static class ConnectionPool {
        private final Semaphore semaphore;
        private final ConcurrentLinkedQueue<DatabaseConnection> availableConnections;
        private final List<DatabaseConnection> allConnections;

        public ConnectionPool(int maxConnections) {
            this.semaphore = new Semaphore(maxConnections, true); // Fair semaphore
            this.availableConnections = new ConcurrentLinkedQueue<>();
            this.allConnections = new ArrayList<>();

            // Create connections
            for (int i = 0; i < maxConnections; i++) {
                DatabaseConnection conn = new DatabaseConnection("CONN-" + (i + 1));
                availableConnections.offer(conn);
                allConnections.add(conn);
            }
        }

        public DatabaseConnection acquireConnection() throws InterruptedException {
            return acquireConnection(0, null); // No timeout
        }

        public DatabaseConnection acquireConnection(long timeout, TimeUnit unit) throws InterruptedException {
            boolean acquired = unit == null ?
                semaphore.acquire() :
                semaphore.tryAcquire(timeout, unit);

            if (acquired) {
                DatabaseConnection conn = availableConnections.poll();
                if (conn != null) {
                    conn.setInUse(true);
                    System.out.println("Acquired connection: " + conn.getConnectionId());
                    return conn;
                } else {
                    semaphore.release(); // Release permit if no connection available
                }
            }
            return null; // Timeout or no connection available
        }

        public void releaseConnection(DatabaseConnection conn) {
            if (conn != null) {
                conn.setInUse(false);
                availableConnections.offer(conn);
                semaphore.release();
                System.out.println("Released connection: " + conn.getConnectionId());
            }
        }

        public void shutdown() {
            for (DatabaseConnection conn : allConnections) {
                conn.close();
            }
            availableConnections.clear();
        }

        public int getAvailableConnections() {
            return availableConnections.size();
        }

        public int getActiveConnections() {
            return allConnections.size() - availableConnections.size();
        }
    }

    static class DatabaseClient implements Runnable {
        private final String clientId;
        private final ConnectionPool pool;
        private final int queries;

        public DatabaseClient(String clientId, ConnectionPool pool, int queries) {
            this.clientId = clientId;
            this.pool = pool;
            this.queries = queries;
        }

        @Override
        public void run() {
            DatabaseConnection conn = null;
            try {
                // Try to acquire connection with timeout
                conn = pool.acquireConnection(5, TimeUnit.SECONDS);

                if (conn == null) {
                    System.err.println(clientId + " failed to acquire connection (timeout)");
                    return;
                }

                // Execute queries
                for (int i = 0; i < queries; i++) {
                    conn.executeQuery("SELECT * FROM users WHERE client = '" + clientId + "' AND query = " + i);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println(clientId + " was interrupted");
            } finally {
                if (conn != null) {
                    pool.releaseConnection(conn);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ConnectionPool pool = new ConnectionPool(3); // Max 3 connections

        // Create multiple clients
        Thread[] clients = new Thread[6];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = new Thread(new DatabaseClient("Client" + (i + 1), pool, 2));
            clients[i].start();
        }

        // Monitor pool status
        while (true) {
            boolean allCompleted = true;
            for (Thread client : clients) {
                if (client.isAlive()) {
                    allCompleted = false;
                    break;
                }
            }

            if (allCompleted) {
                break;
            }

            System.out.println("Pool status - Available: " + pool.getAvailableConnections() +
                             ", Active: " + pool.getActiveConnections());
            Thread.sleep(500);
        }

        // Wait for all clients to complete
        for (Thread client : clients) {
            client.join();
        }

        System.out.println("All clients completed");
        System.out.println("Final pool status - Available: " + pool.getAvailableConnections() +
                         ", Active: " + pool.getActiveConnections());

        pool.shutdown();
    }
}
```
> **Explanation:** Semaphore controls access to limited resources:
acquire(): Blocks until permit available
tryAcquire(): Non-blocking or with timeout
release(): Returns permit to pool
Fairness: Fair semaphore prevents starvation
Resource management: Ensures proper cleanup of acquired resources
### 🧩 Scenario 17: Exchanger for Data Pipeline
> **Problem Statement:** Implement a producer-consumer data pipeline using Exchanger to swap data buffers between threads.
> **Solution:**
```java
import java.util.concurrent.Exchanger;
import java.util.ArrayList;
import java.util.List;

public class DataPipelineExchanger {

    static class DataBuffer {
        private final List<String> data = new ArrayList<>();
        private final int capacity;

        public DataBuffer(int capacity) {
            this.capacity = capacity;
        }

        public void add(String item) {
            if (!isFull()) {
                data.add(item);
            }
        }

        public String remove() {
            if (!isEmpty()) {
                return data.remove(0);
            }
            return null;
        }

        public boolean isFull() {
            return data.size() >= capacity;
        }

        public boolean isEmpty() {
            return data.isEmpty();
        }

        public int size() {
            return data.size();
        }

        public void clear() {
            data.clear();
        }

        @Override
        public String toString() {
            return "DataBuffer[size=" + size() + ", capacity=" + capacity + "]";
        }
    }

    static class Producer implements Runnable {
        private final Exchanger<DataBuffer> exchanger;
        private DataBuffer buffer;
        private final String producerName;
        private volatile boolean running = true;

        public Producer(Exchanger<DataBuffer> exchanger, DataBuffer buffer, String producerName) {
            this.exchanger = exchanger;
            this.buffer = buffer;
            this.producerName = producerName;
        }

        @Override
        public void run() {
            try {
                int itemCounter = 0;

                while (running) {
                    // Produce data until buffer is full
                    while (!buffer.isFull() && running) {
                        String item = "Data-" + producerName + "-" + (itemCounter++);
                        buffer.add(item);
                        System.out.println(producerName + " produced: " + item);
                        Thread.sleep(100); // Simulate production time
                    }

                    System.out.println(producerName + " buffer full, exchanging: " + buffer);

                    // Exchange full buffer for empty one
                    try {
                        buffer = exchanger.exchange(buffer);
                        System.out.println(producerName + " received empty buffer: " + buffer);
                        buffer.clear(); // Ensure it's empty
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println(producerName + " stopped");
        }

        public void stop() {
            running = false;
        }
    }

    static class Consumer implements Runnable {
        private final Exchanger<DataBuffer> exchanger;
        private DataBuffer buffer;
        private final String consumerName;
        private volatile boolean running = true;

        public Consumer(Exchanger<DataBuffer> exchanger, DataBuffer buffer, String consumerName) {
            this.exchanger = exchanger;
            this.buffer = buffer;
            this.consumerName = consumerName;
        }

        @Override
        public void run() {
            try {
                // Start with empty buffer, need to get full one from producer
                buffer = exchanger.exchange(buffer);
                System.out.println(consumerName + " started with buffer: " + buffer);

                while (running) {
                    // Consume data until buffer is empty
                    while (!buffer.isEmpty() && running) {
                        String item = buffer.remove();
                        if (item != null) {
                            System.out.println(consumerName + " consumed: " + item);
                            processItem(item);
                        }
                    }

                    System.out.println(consumerName + " buffer empty, exchanging: " + buffer);

                    // Exchange empty buffer for full one
                    try {
                        buffer = exchanger.exchange(buffer);
                        System.out.println(consumerName + " received full buffer: " + buffer);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println(consumerName + " stopped");
        }

        private void processItem(String item) throws InterruptedException {
            // Simulate processing time
            Thread.sleep(200);
            System.out.println(consumerName + " processed: " + item);
        }

        public void stop() {
            running = false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Create exchanger for DataBuffer objects
        Exchanger<DataBuffer> exchanger = new Exchanger<>();

        // Create buffers
        DataBuffer producerBuffer = new DataBuffer(5);
        DataBuffer consumerBuffer = new DataBuffer(5);

        // Create producer and consumer
        Producer producer = new Producer(exchanger, producerBuffer, "Producer-1");
        Consumer consumer = new Consumer(exchanger, consumerBuffer, "Consumer-1");

        // Start threads
        Thread producerThread = new Thread(producer);
        Thread consumerThread = new Thread(consumer);

        producerThread.start();
        consumerThread.start();

        // Let them run for a while
        Thread.sleep(10000);

        // Stop both threads
        producer.stop();
        consumer.stop();

        // Interrupt if they're stuck in exchange
        producerThread.interrupt();
        consumerThread.interrupt();

        // Wait for completion
        producerThread.join();
        consumerThread.join();

        System.out.println("Data pipeline stopped");
    }
}
```
> **Explanation:** Exchanger enables direct data exchange between threads:
exchange(): Blocks until both threads call exchange
Data pipeline: Efficient for producer-consumer patterns
Buffer swapping: Eliminates need for shared queues
Symmetrical: Both parties must participate in exchange
## 💀 Deadlock & Race Condition Solutions
### 🧩 Scenario 18: Deadlock Detection and Resolution
> **Problem Statement:** Implement a banking system that demonstrates deadlock prevention using lock ordering and try-lock mechanisms.
> **Solution:**
```java
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public class BankingSystemDeadlockPrevention {

    static class BankAccount {
        private final String accountId;
        private double balance;
        private final ReentrantLock lock = new ReentrantLock();

        public BankAccount(String accountId, double initialBalance) {
            this.accountId = accountId;
            this.balance = initialBalance;
        }

        public void deposit(double amount) {
            lock.lock();
            try {
                balance += amount;
                System.out.println("Deposited " + amount + " to " + accountId);
            } finally {
                lock.unlock();
            }
        }

        public void withdraw(double amount) {
            lock.lock();
            try {
                if (balance >= amount) {
                    balance -= amount;
                    System.out.println("Withdrew " + amount + " from " + accountId);
                }
            } finally {
                lock.unlock();
            }
        }

        public double getBalance() {
            lock.lock();
            try {
                return balance;
            } finally {
                lock.unlock();
            }
        }

        public String getAccountId() {
            return accountId;
        }

        public ReentrantLock getLock() {
            return lock;
        }
    }

    // Method 1: Lock ordering to prevent deadlock
    static class OrderedTransfer implements Runnable {
        private final BankAccount fromAccount;
        private final BankAccount toAccount;
        private final double amount;

        public OrderedTransfer(BankAccount fromAccount, BankAccount toAccount, double amount) {
            this.fromAccount = fromAccount;
            this.toAccount = toAccount;
            this.amount = amount;
        }

        @Override
        public void run() {
            // Order accounts by ID to prevent circular wait
            BankAccount firstLock = fromAccount.getAccountId().compareTo(toAccount.getAccountId()) < 0
                ? fromAccount : toAccount;
            BankAccount secondLock = fromAccount.getAccountId().compareTo(toAccount.getAccountId()) < 0
                ? toAccount : fromAccount;

            firstLock.getLock().lock();
            try {
                Thread.sleep(100); // Simulate some processing
                secondLock.getLock().lock();
                try {
                    if (fromAccount.getBalance() >= amount) {
                        fromAccount.withdraw(amount);
                        toAccount.deposit(amount);
                        System.out.println("Transferred " + amount + " from " +
                                         fromAccount.getAccountId() + " to " + toAccount.getAccountId());
                    }
                } finally {
                    secondLock.getLock().unlock();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                firstLock.getLock().unlock();
            }
        }
    }

    // Method 2: Try-lock with timeout
    static class TryLockTransfer implements Runnable {
        private final BankAccount fromAccount;
        private final BankAccount toAccount;
        private final double amount;

        public TryLockTransfer(BankAccount fromAccount, BankAccount toAccount, double amount) {
            this.fromAccount = fromAccount;
            this.toAccount = toAccount;
            this.amount = amount;
        }

        @Override
        public void run() {
            boolean fromLockAcquired = false;
            boolean toLockAcquired = false;

            try {
                // Try to acquire both locks with timeout
                fromLockAcquired = fromAccount.getLock().tryLock(1, TimeUnit.SECONDS);
                if (fromLockAcquired) {
                    toLockAcquired = toAccount.getLock().tryLock(1, TimeUnit.SECONDS);
                    if (toLockAcquired) {
                        // Both locks acquired, perform transfer
                        if (fromAccount.getBalance() >= amount) {
                            fromAccount.withdraw(amount);
                            toAccount.deposit(amount);
                            System.out.println("Transferred " + amount + " from " +
                                             fromAccount.getAccountId() + " to " + toAccount.getAccountId());
                        }
                    } else {
                        System.err.println("Could not acquire toAccount lock for transfer from " +
                                         fromAccount.getAccountId() + " to " + toAccount.getAccountId());
                    }
                } else {
                    System.err.println("Could not acquire fromAccount lock for transfer from " +
                                     fromAccount.getAccountId() + " to " + toAccount.getAccountId());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                if (toLockAcquired) {
                    toAccount.getLock().unlock();
                }
                if (fromLockAcquired) {
                    fromAccount.getLock().unlock();
                }
            }
        }
    }

    // Method 3: Deadlock detection and recovery
    static class DeadlockDetectingTransfer implements Runnable {
        private static final ConcurrentHashMap<Thread, BankAccount> lockMap = new ConcurrentHashMap<>();
        private final BankAccount fromAccount;
        private final BankAccount toAccount;
        private final double amount;

        public DeadlockDetectingTransfer(BankAccount fromAccount, BankAccount toAccount, double amount) {
            this.fromAccount = fromAccount;
            this.toAccount = toAccount;
            this.amount = amount;
        }

        @Override
        public void run() {
            int retries = 3;
            while (retries-- > 0) {
                if (attemptTransfer()) {
                    return;
                }
                // Back off and retry
                try {
                    Thread.sleep((long) (Math.random() * 1000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            System.err.println("Transfer failed after retries");
        }

        private boolean attemptTransfer() {
            // Check for potential deadlock
            Thread currentThread = Thread.currentThread();
            lockMap.put(currentThread, fromAccount);

            // Simple deadlock detection - check if target account is locked by another thread
            for (Map.Entry<Thread, BankAccount> entry : lockMap.entrySet()) {
                if (entry.getKey() != currentThread && entry.getValue() == toAccount) {
                    System.err.println("Potential deadlock detected between " + currentThread.getName() +
                                     " and " + entry.getKey().getName());
                    lockMap.remove(currentThread);
                    return false;
                }
            }

            try {
                fromAccount.getLock().lock();
                try {
                    toAccount.getLock().lock();
                    try {
                        // Perform transfer
                        if (fromAccount.getBalance() >= amount) {
                            fromAccount.withdraw(amount);
                            toAccount.deposit(amount);
                            System.out.println("Transferred " + amount + " from " +
                                             fromAccount.getAccountId() + " to " + toAccount.getAccountId());
                            return true;
                        }
                    } finally {
                        toAccount.getLock().unlock();
                    }
                } finally {
                    fromAccount.getLock().unlock();
                }
            } finally {
                lockMap.remove(currentThread);
            }
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        BankAccount account1 = new BankAccount("ACC-001", 1000);
        BankAccount account2 = new BankAccount("ACC-002", 1000);

        System.out.println("=== Testing Lock Ordering (Deadlock Prevention) ===");
        // This should work without deadlock due to lock ordering
        Thread t1 = new Thread(new OrderedTransfer(account1, account2, 100), "Transfer-1");
        Thread t2 = new Thread(new OrderedTransfer(account2, account1, 150), "Transfer-2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Account balances after ordered transfers:");
        System.out.println("Account 1: " + account1.getBalance());
        System.out.println("Account 2: " + account2.getBalance());

        System.out.println("\n=== Testing Try-Lock with Timeout ===");
        // Reset balances
        account1 = new BankAccount("ACC-001", 1000);
        account2 = new BankAccount("ACC-002", 1000);

        Thread t3 = new Thread(new TryLockTransfer(account1, account2, 200), "TryTransfer-1");
        Thread t4 = new Thread(new TryLockTransfer(account2, account1, 250), "TryTransfer-2");

        t3.start();
        t4.start();
        t3.join();
        t4.join();

        System.out.println("Account balances after try-lock transfers:");
        System.out.println("Account 1: " + account1.getBalance());
        System.out.println("Account 2: " + account2.getBalance());

        System.out.println("\n=== Testing Deadlock Detection ===");
        account1 = new BankAccount("ACC-001", 1000);
        account2 = new BankAccount("ACC-002", 1000);

        Thread t5 = new Thread(new DeadlockDetectingTransfer(account1, account2, 300), "DetectTransfer-1");
        Thread t6 = new Thread(new DeadlockDetectingTransfer(account2, account1, 350), "DetectTransfer-2");

        t5.start();
        t6.start();
        t5.join();
        t6.join();

        System.out.println("Account balances after deadlock detection transfers:");
        System.out.println("Account 1: " + account1.getBalance());
        System.out.println("Account 2: " + account2.getBalance());
    }
}
```
> **Explanation:** Deadlock prevention strategies:
Lock ordering: Always acquire locks in consistent order
Try-lock with timeout: Attempt to acquire locks with timeout
Deadlock detection: Monitor for circular wait conditions
Lock hierarchy: Use lock ordering based on object hierarchy
### 🧩 Scenario 19: Race Condition in Ticket Booking System
> **Problem Statement:** Build a ticket booking system that demonstrates race conditions and their solutions using atomic operations.
> **Solution:**
```java
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TicketBookingSystem {

    // Unsafe implementation showing race conditions
    static class UnsafeTicketSystem {
        private int availableTickets;
        private final String eventName;

        public UnsafeTicketSystem(String eventName, int totalTickets) {
            this.eventName = eventName;
            this.availableTickets = totalTickets;
        }

        public boolean bookTicket(String customerName, int quantity) {
            if (availableTickets >= quantity) {
                try {
                    Thread.sleep(100); // Simulate booking process
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                availableTickets -= quantity;
                System.out.println(customerName + " booked " + quantity + " tickets for " + eventName);
                return true;
            }
            System.out.println(customerName + " could not book tickets - insufficient availability");
            return false;
        }

        public int getAvailableTickets() {
            return availableTickets;
        }
    }

    // Safe implementation using synchronized
    static class SynchronizedTicketSystem {
        private int availableTickets;
        private final String eventName;

        public SynchronizedTicketSystem(String eventName, int totalTickets) {
            this.eventName = eventName;
            this.availableTickets = totalTickets;
        }

        public synchronized boolean bookTicket(String customerName, int quantity) {
            if (availableTickets >= quantity) {
                try {
                    Thread.sleep(100); // Simulate booking process
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                availableTickets -= quantity;
                System.out.println(customerName + " booked " + quantity + " tickets for " + eventName);
                return true;
            }
            System.out.println(customerName + " could not book tickets - insufficient availability");
            return false;
        }

        public synchronized int getAvailableTickets() {
            return availableTickets;
        }
    }

    // Safe implementation using AtomicInteger
    static class AtomicTicketSystem {
        private final AtomicInteger availableTickets;
        private final String eventName;

        public AtomicTicketSystem(String eventName, int totalTickets) {
            this.eventName = eventName;
            this.availableTickets = new AtomicInteger(totalTickets);
        }

        public boolean bookTicket(String customerName, int quantity) {
            while (true) {
                int current = availableTickets.get();
                if (current >= quantity) {
                    int next = current - quantity;
                    if (availableTickets.compareAndSet(current, next)) {
                        System.out.println(customerName + " booked " + quantity + " tickets for " + eventName);
                        return true;
                    }
                    // Retry if CAS failed
                } else {
                    System.out.println(customerName + " could not book tickets - insufficient availability");
                    return false;
                }
            }
        }

        public int getAvailableTickets() {
            return availableTickets.get();
        }
    }

    // Advanced implementation with booking state tracking
    static class AdvancedTicketSystem {
        private final AtomicInteger availableTickets;
        private final AtomicInteger totalBooked;
        private final AtomicBoolean bookingOpen;
        private final String eventName;
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        public AdvancedTicketSystem(String eventName, int totalTickets) {
            this.eventName = eventName;
            this.availableTickets = new AtomicInteger(totalTickets);
            this.totalBooked = new AtomicInteger(0);
            this.bookingOpen = new AtomicBoolean(true);
        }

        public BookingResult bookTicket(String customerName, int quantity) {
            if (!bookingOpen.get()) {
                return new BookingResult(false, 0, "Booking is closed");
            }

            if (quantity <= 0) {
                return new BookingResult(false, 0, "Invalid quantity");
            }

            // Optimistic booking attempt
            int bookedQuantity = 0;
            int remainingNeeded = quantity;

            while (remainingNeeded > 0) {
                int currentAvailable = availableTickets.get();

                if (currentAvailable <= 0) {
                    break;
                }

                int canBook = Math.min(remainingNeeded, currentAvailable);
                int nextAvailable = currentAvailable - canBook;

                if (availableTickets.compareAndSet(currentAvailable, nextAvailable)) {
                    bookedQuantity += canBook;
                    remainingNeeded -= canBook;
                    totalBooked.addAndGet(canBook);
                }
            }

            if (bookedQuantity > 0) {
                String message = customerName + " successfully booked " + bookedQuantity +
                               " tickets for " + eventName;
                if (remainingNeeded > 0) {
                    message += " (requested " + quantity + ", but only " + bookedQuantity + " available)";
                }
                return new BookingResult(true, bookedQuantity, message);
            } else {
                return new BookingResult(false, 0, "No tickets available");
            }
        }

        public BookingStatistics getStatistics() {
            lock.readLock().lock();
            try {
                return new BookingStatistics(
                    availableTickets.get(),
                    totalBooked.get(),
                    bookingOpen.get()
                );
            } finally {
                lock.readLock().unlock();
            }
        }

        public void closeBooking() {
            bookingOpen.set(false);
            System.out.println("Booking closed for " + eventName);
        }

        public void reopenBooking() {
            bookingOpen.set(true);
            System.out.println("Booking reopened for " + eventName);
        }
    }

    static class BookingResult {
        private final boolean success;
        private final int quantityBooked;
        private final String message;

        public BookingResult(boolean success, int quantityBooked, String message) {
            this.success = success;
            this.quantityBooked = quantityBooked;
            this.message = message;
        }

        @Override
        public String toString() {
            return "BookingResult[success=" + success + ", quantity=" + quantityBooked + ", message=" + message + "]";
        }
    }

    static class BookingStatistics {
        private final int availableTickets;
        private final int totalBooked;
        private final boolean bookingOpen;

        public BookingStatistics(int availableTickets, int totalBooked, boolean bookingOpen) {
            this.availableTickets = availableTickets;
            this.totalBooked = totalBooked;
            this.bookingOpen = bookingOpen;
        }

        @Override
        public String toString() {
            return "BookingStats[available=" + availableTickets + ", booked=" + totalBooked + ", open=" + bookingOpen + "]";
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Unsafe Ticket System (Race Condition) ===");
        UnsafeTicketSystem unsafeSystem = new UnsafeTicketSystem("Concert", 100);

        Thread[] unsafeThreads = new Thread[10];
        for (int i = 0; i < unsafeThreads.length; i++) {
            final int customerId = i;
            unsafeThreads[i] = new Thread(() -> {
                unsafeSystem.bookTicket("Customer" + customerId, 15);
            });
        }

        for (Thread thread : unsafeThreads) {
            thread.start();
        }
        for (Thread thread : unsafeThreads) {
            thread.join();
        }

        System.out.println("Unsafe system remaining tickets: " + unsafeSystem.getAvailableTickets());

        System.out.println("\n=== Synchronized Ticket System ===");
        SynchronizedTicketSystem syncSystem = new SynchronizedTicketSystem("Concert", 100);

        Thread[] syncThreads = new Thread[10];
        for (int i = 0; i < syncThreads.length; i++) {
            final int customerId = i;
            syncThreads[i] = new Thread(() -> {
                syncSystem.bookTicket("Customer" + customerId, 15);
            });
        }

        for (Thread thread : syncThreads) {
            thread.start();
        }
        for (Thread thread : syncThreads) {
            thread.join();
        }

        System.out.println("Synchronized system remaining tickets: " + syncSystem.getAvailableTickets());

        System.out.println("\n=== Atomic Ticket System ===");
        AtomicTicketSystem atomicSystem = new AtomicTicketSystem("Concert", 100);

        Thread[] atomicThreads = new Thread[10];
        for (int i = 0; i < atomicThreads.length; i++) {
            final int customerId = i;
            atomicThreads[i] = new Thread(() -> {
                atomicSystem.bookTicket("Customer" + customerId, 15);
            });
        }

        for (Thread thread : atomicThreads) {
            thread.start();
        }
        for (Thread thread : atomicThreads) {
            thread.join();
        }

        System.out.println("Atomic system remaining tickets: " + atomicSystem.getAvailableTickets());

        System.out.println("\n=== Advanced Ticket System ===");
        AdvancedTicketSystem advancedSystem = new AdvancedTicketSystem("Concert", 100);

        Thread[] advancedThreads = new Thread[15];
        for (int i = 0; i < advancedThreads.length; i++) {
            final int customerId = i;
            advancedThreads[i] = new Thread(() -> {
                BookingResult result = advancedSystem.bookTicket("Customer" + customerId, 8);
                System.out.println(result);
            });
        }

        for (Thread thread : advancedThreads) {
            thread.start();
        }
        for (Thread thread : advancedThreads) {
            thread.join();
        }

        System.out.println("Final statistics: " + advancedSystem.getStatistics());
    }
}
```
> **Explanation:** Race condition solutions:
Synchronized methods: Simple but can be bottleneck
Atomic operations: Lock-free, better performance for simple operations
Compare-and-swap: Retry mechanism for atomic updates
Advanced atomic classes: AtomicInteger, AtomicLong, AtomicReference
## 📡 Thread Communication
### 🧩 Scenario 20: Producer-Consumer with wait/notify
> **Problem Statement:** Implement a classic producer-consumer pattern using wait() and notify() for thread communication.
> **Solution:**
```java
import java.util.LinkedList;
import java.util.Queue;

public class ProducerConsumerWaitNotify {

    static class SharedBuffer {
        private final Queue<String> buffer = new LinkedList<>();
        private final int capacity;

        public SharedBuffer(int capacity) {
            this.capacity = capacity;
        }

        public synchronized void produce(String item) throws InterruptedException {
            // Wait while buffer is full
            while (buffer.size() == capacity) {
                System.out.println("Buffer full, producer waiting...");
                wait();
            }

            // Produce item
            buffer.offer(item);
            System.out.println("Produced: " + item + " (Buffer size: " + buffer.size() + ")");

            // Notify waiting consumers
            notifyAll();
        }

        public synchronized String consume() throws InterruptedException {
            // Wait while buffer is empty
            while (buffer.isEmpty()) {
                System.out.println("Buffer empty, consumer waiting...");
                wait();
            }

            // Consume item
            String item = buffer.poll();
            System.out.println("Consumed: " + item + " (Buffer size: " + buffer.size() + ")");

            // Notify waiting producers
            notifyAll();
            return item;
        }

        public synchronized int getSize() {
            return buffer.size();
        }
    }

    static class Producer implements Runnable {
        private final SharedBuffer buffer;
        private final String producerName;
        private final int itemCount;

        public Producer(SharedBuffer buffer, String producerName, int itemCount) {
            this.buffer = buffer;
            this.producerName = producerName;
            this.itemCount = itemCount;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < itemCount; i++) {
                    String item = "Item-" + producerName + "-" + i;
                    buffer.produce(item);
                    Thread.sleep((long) (Math.random() * 1000)); // Simulate production time
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println(producerName + " was interrupted");
            }
        }
    }

    static class Consumer implements Runnable {
        private final SharedBuffer buffer;
        private final String consumerName;
        private final int itemCount;

        public Consumer(SharedBuffer buffer, String consumerName, int itemCount) {
            this.buffer = buffer;
            this.consumerName = consumerName;
            this.itemCount = itemCount;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < itemCount; i++) {
                    String item = buffer.consume();
                    // Process item
                    Thread.sleep((long) (Math.random() * 1500)); // Simulate consumption time
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println(consumerName + " was interrupted");
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SharedBuffer buffer = new SharedBuffer(5);

        // Create multiple producers and consumers
        Thread producer1 = new Thread(new Producer(buffer, "Producer-1", 10));
        Thread producer2 = new Thread(new Producer(buffer, "Producer-2", 8));
        Thread consumer1 = new Thread(new Consumer(buffer, "Consumer-1", 12));
        Thread consumer2 = new Thread(new Consumer(buffer, "Consumer-2", 6));

        // Start all threads
        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();

        // Wait for completion
        producer1.join();
        producer2.join();
        consumer1.join();
        consumer2.join();

        System.out.println("All producers and consumers completed");
    }
}
```
> **Explanation:** wait() and notify() mechanism:
wait(): Releases lock and waits for notification
notify(): Wakes up one waiting thread
notifyAll(): Wakes up all waiting threads
while loop: Always check condition in loop to handle spurious wakeups
synchronized: Must be called within synchronized block/method
### 🧩 Scenario 21: Event-Driven Architecture with BlockingQueue
> **Problem Statement:** Build an event-driven system where components communicate through event queues.
> **Solution:**
```java
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class EventDrivenArchitecture {

    // Event types
    enum EventType {
        USER_LOGIN, USER_LOGOUT, ORDER_CREATED, ORDER_CANCELLED, PAYMENT_PROCESSED
    }

    // Event class
    static class Event {
        private final EventType type;
        private final String data;
        private final long timestamp;

        public Event(EventType type, String data) {
            this.type = type;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        public EventType getType() { return type; }
        public String getData() { return data; }
        public long getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return "Event[type=" + type + ", data=" + data + ", time=" + timestamp + "]";
        }
    }

    // Event handler interface
    interface EventHandler {
        void handleEvent(Event event);
        boolean canHandle(EventType type);
    }

    // User service handler
    static class UserService implements EventHandler, Runnable {
        private final BlockingQueue<Event> eventQueue;
        private volatile boolean running = true;

        public UserService(BlockingQueue<Event> eventQueue) {
            this.eventQueue = eventQueue;
        }

        @Override
        public void handleEvent(Event event) {
            System.out.println("UserService handling: " + event);

            switch (event.getType()) {
                case USER_LOGIN:
                    processUserLogin(event.getData());
                    break;
                case USER_LOGOUT:
                    processUserLogout(event.getData());
                    break;
                default:
                    System.err.println("UserService cannot handle: " + event.getType());
            }
        }

        @Override
        public boolean canHandle(EventType type) {
            return type == EventType.USER_LOGIN || type == EventType.USER_LOGOUT;
        }

        @Override
        public void run() {
            try {
                while (running) {
                    Event event = eventQueue.poll(1, TimeUnit.SECONDS);
                    if (event != null && canHandle(event.getType())) {
                        handleEvent(event);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void processUserLogin(String userData) {
            System.out.println("Processing user login: " + userData);
            // Simulate processing
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void processUserLogout(String userData) {
            System.out.println("Processing user logout: " + userData);
            // Simulate processing
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void stop() {
            running = false;
        }
    }

    // Order service handler
    static class OrderService implements EventHandler, Runnable {
        private final BlockingQueue<Event> eventQueue;
        private volatile boolean running = true;

        public OrderService(BlockingQueue<Event> eventQueue) {
            this.eventQueue = eventQueue;
        }

        @Override
        public void handleEvent(Event event) {
            System.out.println("OrderService handling: " + event);

            switch (event.getType()) {
                case ORDER_CREATED:
                    processOrderCreated(event.getData());
                    break;
                case ORDER_CANCELLED:
                    processOrderCancelled(event.getData());
                    break;
                default:
                    System.err.println("OrderService cannot handle: " + event.getType());
            }
        }

        @Override
        public boolean canHandle(EventType type) {
            return type == EventType.ORDER_CREATED || type == EventType.ORDER_CANCELLED;
        }

        @Override
        public void run() {
            try {
                while (running) {
                    Event event = eventQueue.poll(1, TimeUnit.SECONDS);
                    if (event != null && canHandle(event.getType())) {
                        handleEvent(event);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void processOrderCreated(String orderData) {
            System.out.println("Processing order creation: " + orderData);
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void processOrderCancelled(String orderData) {
            System.out.println("Processing order cancellation: " + orderData);
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void stop() {
            running = false;
        }
    }

    // Event bus for distributing events
    static class EventBus {
        private final BlockingQueue<Event> eventQueue;
        private final List<EventHandler> handlers;
        private final ExecutorService executor;

        public EventBus(int queueCapacity) {
            this.eventQueue = new LinkedBlockingQueue<>(queueCapacity);
            this.handlers = new CopyOnWriteArrayList<>();
            this.executor = Executors.newCachedThreadPool();
        }

        public void registerHandler(EventHandler handler) {
            handlers.add(handler);
            executor.submit(handler); // Start handler in separate thread
        }

        public void publishEvent(Event event) throws InterruptedException {
            boolean offered = eventQueue.offer(event, 1, TimeUnit.SECONDS);
            if (!offered) {
                System.err.println("Event queue full, dropping event: " + event);
            }
        }

        public void publishEvents(List<Event> events) throws InterruptedException {
            for (Event event : events) {
                publishEvent(event);
            }
        }

        public void shutdown() throws InterruptedException {
            // Stop all handlers
            for (EventHandler handler : handlers) {
                if (handler instanceof Runnable) {
                    ((Runnable) handler).stop();
                }
            }

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        public int getQueueSize() {
            return eventQueue.size();
        }
    }

    // Event producer
    static class EventProducer implements Runnable {
        private final EventBus eventBus;
        private volatile boolean running = true;

        public EventProducer(EventBus eventBus) {
            this.eventBus = eventBus;
        }

        @Override
        public void run() {
            try {
                int eventCounter = 0;
                while (running) {
                    // Generate random events
                    EventType[] types = EventType.values();
                    EventType randomType = types[(int) (Math.random() * types.length)];
                    String data = "Data-" + (eventCounter++);

                    Event event = new Event(randomType, data);
                    System.out.println("Producing event: " + event);

                    eventBus.publishEvent(event);
                    Thread.sleep((long) (Math.random() * 1000)); // Random delay
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void stop() {
            running = false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Create event bus
        EventBus eventBus = new EventBus(100);

        // Create and register handlers
        UserService userService = new UserService(eventBus.eventQueue);
        OrderService orderService = new OrderService(eventBus.eventQueue);

        eventBus.registerHandler(userService);
        eventBus.registerHandler(orderService);

        // Create and start producer
        EventProducer producer = new EventProducer(eventBus);
        Thread producerThread = new Thread(producer);
        producerThread.start();

        // Let system run for a while
        Thread.sleep(10000);

        // Stop the system
        System.out.println("Stopping event system...");
        producer.stop();
        producerThread.join();

        eventBus.shutdown();

        System.out.println("Event system stopped");
    }
}
```
> **Explanation:** Event-driven architecture benefits:
Loose coupling: Components communicate through events
Scalability: Handlers can process events concurrently
Flexibility: Easy to add new event types and handlers
Resilience: Failure in one handler doesn't affect others
Queue-based: Handles load spikes with buffering
### 🧩 Scenario 22: Thread-Local for Request Context
> **Problem Statement:** Implement a web request processing system where each thread maintains its own request context without synchronization overhead.
> **Solution:**
```java
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class ThreadLocalRequestContext {

    // Request context class
    static class RequestContext {
        private String requestId;
        private String userId;
        private long startTime;
        private Map<String, Object> attributes = new ConcurrentHashMap<>();

        public RequestContext(String requestId, String userId) {
            this.requestId = requestId;
            this.userId = userId;
            this.startTime = System.currentTimeMillis();
        }

        public String getRequestId() { return requestId; }
        public String getUserId() { return userId; }
        public long getStartTime() { return startTime; }

        public void setAttribute(String key, Object value) {
            attributes.put(key, value);
        }

        public Object getAttribute(String key) {
            return attributes.get(key);
        }

        public long getElapsedTime() {
            return System.currentTimeMillis() - startTime;
        }

        @Override
        public String toString() {
            return "RequestContext[requestId=" + requestId + ", userId=" + userId +
                   ", elapsed=" + getElapsedTime() + "ms]";
        }
    }

    // ThreadLocal to store request context
    static class RequestContextHolder {
        private static final ThreadLocal<RequestContext> contextHolder = new ThreadLocal<>();

        public static void setContext(RequestContext context) {
            contextHolder.set(context);
        }

        public static RequestContext getContext() {
            return contextHolder.get();
        }

        public static void clearContext() {
            contextHolder.remove();
        }

        public static boolean hasContext() {
            return contextHolder.get() != null;
        }
    }

    // Service layer that uses request context
    static class UserService {
        public String getCurrentUser() {
            RequestContext context = RequestContextHolder.getContext();
            if (context != null) {
                return context.getUserId();
            }
            return "anonymous";
        }

        public void logUserActivity(String activity) {
            RequestContext context = RequestContextHolder.getContext();
            if (context != null) {
                System.out.println("[" + context.getRequestId() + "] User " +
                                 context.getUserId() + " performed: " + activity +
                                 " (elapsed: " + context.getElapsedTime() + "ms)");
            }
        }
    }

    // Database service with connection pooling per thread
    static class DatabaseService {
        private static final ThreadLocal<DatabaseConnection> connectionHolder =
            ThreadLocal.withInitial(() -> {
                System.out.println("Creating new database connection for thread: " +
                                 Thread.currentThread().getName());
                return new DatabaseConnection("conn-" + Thread.currentThread().getId());
            });

        public static DatabaseConnection getConnection() {
            return connectionHolder.get();
        }

        public static void closeConnection() {
            DatabaseConnection conn = connectionHolder.get();
            if (conn != null) {
                conn.close();
                connectionHolder.remove();
            }
        }

        public void executeQuery(String query) {
            DatabaseConnection conn = getConnection();
            RequestContext context = RequestContextHolder.getContext();

            if (context != null) {
                context.setAttribute("lastQuery", query);
            }

            conn.executeQuery(query);
        }
    }

    // Database connection class
    static class DatabaseConnection {
        private final String connectionId;
        private boolean closed = false;

        public DatabaseConnection(String connectionId) {
            this.connectionId = connectionId;
        }

        public void executeQuery(String query) {
            if (closed) {
                throw new IllegalStateException("Connection already closed");
            }

            RequestContext context = RequestContextHolder.getContext();
            String requestId = context != null ? context.getRequestId() : "unknown";

            System.out.println("[" + requestId + "] Executing query on " + connectionId + ": " + query);

            // Simulate query execution
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void close() {
            closed = true;
            System.out.println("Closed database connection: " + connectionId);
        }
    }

    // Request processor that coordinates everything
    static class RequestProcessor implements Runnable {
        private final String requestId;
        private final String userId;
        private final UserService userService;
        private final DatabaseService databaseService;

        public RequestProcessor(String requestId, String userId) {
            this.requestId = requestId;
            this.userId = userId;
            this.userService = new UserService();
            this.databaseService = new DatabaseService();
        }

        @Override
        public void run() {
            try {
                // Set up request context
                RequestContext context = new RequestContext(requestId, userId);
                RequestContextHolder.setContext(context);

                System.out.println("Processing request: " + context);

                // Simulate request processing
                userService.logUserActivity("login");

                databaseService.executeQuery("SELECT * FROM users WHERE id = '" + userId + "'");
                databaseService.executeQuery("UPDATE users SET last_login = NOW() WHERE id = '" + userId + "'");

                userService.logUserActivity("data_access");

                // Simulate some business logic
                Thread.sleep(500);

                System.out.println("Completed request: " + context);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // Clean up
                RequestContextHolder.clearContext();
                DatabaseService.closeConnection();
            }
        }
    }

    // Custom ThreadLocal with initial value
    static class RequestCounter {
        private static final ThreadLocal<Integer> counter = ThreadLocal.withInitial(() -> 0);

        public static void increment() {
            counter.set(counter.get() + 1);
        }

        public static int get() {
            return counter.get();
        }

        public static void reset() {
            counter.set(0);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Single Thread Example ===");
        RequestProcessor processor = new RequestProcessor("REQ-001", "user123");
        processor.run();

        System.out.println("\n=== Multi-threaded Example ===");
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            final int requestNum = i;
            threads[i] = new Thread(() -> {
                RequestProcessor p = new RequestProcessor("REQ-" + (100 + requestNum), "user" + requestNum);
                p.run();
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("\n=== ThreadLocal Request Counter Example ===");
        Thread[] counterThreads = new Thread[3];
        for (int i = 0; i < counterThreads.length; i++) {
            final int threadNum = i;
            counterThreads[i] = new Thread(() -> {
                String threadName = "Thread-" + threadNum;
                System.out.println(threadName + " initial counter: " + RequestCounter.get());

                for (int j = 0; j < 5; j++) {
                    RequestCounter.increment();
                    System.out.println(threadName + " counter after increment " + (j + 1) + ": " + RequestCounter.get());
                }

                System.out.println(threadName + " final counter: " + RequestCounter.get());
            });
        }

        for (Thread thread : counterThreads) {
            thread.start();
        }

        for (Thread thread : counterThreads) {
            thread.join();
        }

        System.out.println("All threads completed");
    }
}
```
> **Explanation:** ThreadLocal provides thread-local variables:
Thread isolation: Each thread has its own copy
No synchronization: No need for locks
Automatic cleanup: Remove() prevents memory leaks
withInitial(): Provides initial value factory
Use cases: Request context, database connections, user sessions
## 🌍 Real-World Scenarios
### 🧩 Scenario 23: Stock Trading System
> **Problem Statement:** Build a multi-threaded stock trading system with real-time price updates, order processing, and risk management.
> **Solution:**
```java
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;

public class StockTradingSystem {

    // Stock price update
    static class StockPrice {
        private final String symbol;
        private final AtomicDouble price;
        private final AtomicLong lastUpdated;

        public StockPrice(String symbol, double initialPrice) {
            this.symbol = symbol;
            this.price = new AtomicDouble(initialPrice);
            this.lastUpdated = new AtomicLong(System.currentTimeMillis());
        }

        public void updatePrice(double newPrice) {
            price.set(newPrice);
            lastUpdated.set(System.currentTimeMillis());
        }

        public double getPrice() {
            return price.get();
        }

        public String getSymbol() {
            return symbol;
        }

        public long getLastUpdated() {
            return lastUpdated.get();
        }
    }

    // Order types
    enum OrderType { BUY, SELL }
    enum OrderStatus { PENDING, EXECUTED, CANCELLED }

    // Trading order
    static class Order {
        private final String orderId;
        private final String symbol;
        private final OrderType type;
        private final int quantity;
        private final double price;
        private final AtomicReference<OrderStatus> status;
        private final long createdTime;

        public Order(String orderId, String symbol, OrderType type, int quantity, double price) {
            this.orderId = orderId;
            this.symbol = symbol;
            this.type = type;
            this.quantity = quantity;
            this.price = price;
            this.status = new AtomicReference<>(OrderStatus.PENDING);
            this.createdTime = System.currentTimeMillis();
        }

        public boolean execute() {
            return status.compareAndSet(OrderStatus.PENDING, OrderStatus.EXECUTED);
        }

        public boolean cancel() {
            return status.compareAndSet(OrderStatus.PENDING, OrderStatus.CANCELLED);
        }

        // Getters
        public String getOrderId() { return orderId; }
        public String getSymbol() { return symbol; }
        public OrderType getType() { return type; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public OrderStatus getStatus() { return status.get(); }
        public long getCreatedTime() { return createdTime; }
    }

    // Market data service
    static class MarketDataService implements Runnable {
        private final Map<String, StockPrice> stockPrices;
        private final List<Order> orderBook;
        private final ExecutorService orderProcessor;
        private volatile boolean running = true;

        public MarketDataService() {
            this.stockPrices = new ConcurrentHashMap<>();
            this.orderBook = new CopyOnWriteArrayList<>();
            this.orderProcessor = Executors.newFixedThreadPool(10);

            // Initialize some stocks
            stockPrices.put("AAPL", new StockPrice("AAPL", 150.0));
            stockPrices.put("GOOGL", new StockPrice("GOOGL", 2800.0));
            stockPrices.put("MSFT", new StockPrice("MSFT", 300.0));
            stockPrices.put("TSLA", new StockPrice("TSLA", 800.0));
        }

        public void submitOrder(Order order) {
            orderBook.add(order);
            System.out.println("Order submitted: " + order.getOrderId() + " " + order.getType() +
                             " " + order.getQuantity() + " shares of " + order.getSymbol() + " at $" + order.getPrice());

            // Process order asynchronously
            orderProcessor.submit(() -> processOrder(order));
        }

        private void processOrder(Order order) {
            StockPrice stock = stockPrices.get(order.getSymbol());
            if (stock == null) {
                order.cancel();
                return;
            }

            // Simulate order processing delay
            try {
                Thread.sleep(100 + (long) (Math.random() * 400));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            double currentPrice = stock.getPrice();

            // Simple price matching logic
            boolean canExecute = false;
            if (order.getType() == OrderType.BUY && currentPrice <= order.getPrice()) {
                canExecute = true;
            } else if (order.getType() == OrderType.SELL && currentPrice >= order.getPrice()) {
                canExecute = true;
            }

            if (canExecute && order.execute()) {
                System.out.println("Order executed: " + order.getOrderId() + " at $" + currentPrice);
            } else {
                order.cancel();
                System.out.println("Order cancelled: " + order.getOrderId());
            }
        }

        @Override
        public void run() {
            Random random = new Random();

            while (running) {
                try {
                    // Update stock prices randomly
                    for (StockPrice stock : stockPrices.values()) {
                        double currentPrice = stock.getPrice();
                        double priceChange = (random.nextDouble() - 0.5) * 10; // ±5 change
                        double newPrice = Math.max(0.01, currentPrice + priceChange);
                        stock.updatePrice(newPrice);

                        System.out.println("Price update: " + stock.getSymbol() + " = $" +
                                         String.format("%.2f", newPrice));
                    }

                    Thread.sleep(1000); // Update every second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        public void stop() {
            running = false;
            orderProcessor.shutdown();
        }

        public StockPrice getStockPrice(String symbol) {
            return stockPrices.get(symbol);
        }
    }

    // Risk management service
    static class RiskManagementService implements Runnable {
        private final Map<String, AtomicInteger> positionLimits;
        private final Map<String, AtomicInteger> currentPositions;
        private final BlockingQueue<Order> riskCheckQueue;
        private volatile boolean running = true;

        public RiskManagementService() {
            this.positionLimits = new ConcurrentHashMap<>();
            this.currentPositions = new ConcurrentHashMap<>();
            this.riskCheckQueue = new LinkedBlockingQueue<>();

            // Set position limits
            positionLimits.put("AAPL", new AtomicInteger(1000));
            positionLimits.put("GOOGL", new AtomicInteger(500));
            positionLimits.put("MSFT", new AtomicInteger(800));
            positionLimits.put("TSLA", new AtomicInteger(600));

            // Initialize current positions
            for (String symbol : positionLimits.keySet()) {
                currentPositions.put(symbol, new AtomicInteger(0));
            }
        }

        public boolean checkOrderRisk(Order order) {
            try {
                return riskCheckQueue.offer(order, 1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        @Override
        public void run() {
            try {
                while (running) {
                    Order order = riskCheckQueue.poll(1, TimeUnit.SECONDS);
                    if (order != null) {
                        evaluateRisk(order);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void evaluateRisk(Order order) {
            String symbol = order.getSymbol();
            AtomicInteger currentPosition = currentPositions.get(symbol);
            AtomicInteger positionLimit = positionLimits.get(symbol);

            if (currentPosition == null || positionLimit == null) {
                order.cancel();
                System.err.println("Order rejected - unknown symbol: " + symbol);
                return;
            }

            int current = currentPosition.get();
            int limit = positionLimit.get();
            int orderQuantity = order.getQuantity();

            // Calculate new position
            int newPosition;
            if (order.getType() == OrderType.BUY) {
                newPosition = current + orderQuantity;
            } else {
                newPosition = current - orderQuantity;
            }

            // Check if new position exceeds limits
            if (Math.abs(newPosition) > limit) {
                order.cancel();
                System.err.println("Order rejected - position limit exceeded. Symbol: " + symbol +
                                 ", Current: " + current + ", New: " + newPosition + ", Limit: " + limit);
            } else {
                // Update position
                currentPosition.set(newPosition);
                System.out.println("Risk check passed for " + order.getOrderId() +
                                 ". New position for " + symbol + ": " + newPosition);
            }
        }

        public void stop() {
            running = false;
        }

        public int getCurrentPosition(String symbol) {
            AtomicInteger position = currentPositions.get(symbol);
            return position != null ? position.get() : 0;
        }
    }

    // Trading client
    static class TradingClient implements Runnable {
        private final String clientId;
        private final MarketDataService marketDataService;
        private final RiskManagementService riskService;
        private final AtomicInteger orderCounter;

        public TradingClient(String clientId, MarketDataService marketDataService,
                           RiskManagementService riskService) {
            this.clientId = clientId;
            this.marketDataService = marketDataService;
            this.riskService = riskService;
            this.orderCounter = new AtomicInteger(0);
        }

        @Override
        public void run() {
            Random random = new Random();
            String[] symbols = {"AAPL", "GOOGL", "MSFT", "TSLA"};

            for (int i = 0; i < 5; i++) {
                try {
                    // Generate random order
                    String symbol = symbols[random.nextInt(symbols.length)];
                    OrderType type = random.nextBoolean() ? OrderType.BUY : OrderType.SELL;
                    int quantity = 10 + random.nextInt(90); // 10-100 shares

                    // Get current market price
                    StockPrice stock = marketDataService.getStockPrice(symbol);
                    if (stock == null) continue;

                    double currentPrice = stock.getPrice();
                    double orderPrice = type == OrderType.BUY ?
                        currentPrice * 1.02 : // 2% above market
                        currentPrice * 0.98;  // 2% below market

                    String orderId = clientId + "-" + orderCounter.incrementAndGet();
                    Order order = new Order(orderId, symbol, type, quantity, orderPrice);

                    // Check risk first
                    if (riskService.checkOrderRisk(order)) {
                        // Submit to market
                        marketDataService.submitOrder(order);
                    }

                    Thread.sleep(1000 + random.nextInt(2000)); // Wait between orders

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Create services
        MarketDataService marketDataService = new MarketDataService();
        RiskManagementService riskService = new RiskManagementService();

        // Start services
        Thread marketThread = new Thread(marketDataService, "MarketDataService");
        Thread riskThread = new Thread(riskService, "RiskManagementService");

        marketThread.start();
        riskThread.start();

        // Create and start trading clients
        int clientCount = 3;
        Thread[] clientThreads = new Thread[clientCount];

        for (int i = 0; i < clientCount; i++) {
            TradingClient client = new TradingClient("Client" + (i + 1), marketDataService, riskService);
            clientThreads[i] = new Thread(client, "TradingClient-" + (i + 1));
            clientThreads[i].start();
        }

        // Let the system run
        Thread.sleep(15000);

        // Stop the system
        System.out.println("Stopping trading system...");
        marketDataService.stop();
        riskService.stop();

        // Wait for all threads to complete
        marketThread.join();
        riskThread.join();

        for (Thread clientThread : clientThreads) {
            clientThread.join();
        }

        // Print final statistics
        System.out.println("\n=== Final Statistics ===");
        String[] symbols = {"AAPL", "GOOGL", "MSFT", "TSLA"};
        for (String symbol : symbols) {
            StockPrice stock = marketDataService.getStockPrice(symbol);
            if (stock != null) {
                System.out.println(symbol + " final price: $" +
                                 String.format("%.2f", stock.getPrice()));
            }
            System.out.println(symbol + " position: " + riskService.getCurrentPosition(symbol));
        }

        System.out.println("Trading system stopped");
    }
}
```
> **Explanation:** Stock trading system demonstrates:
Real-time updates: Concurrent price updates
Order processing: Asynchronous order matching
Risk management: Position tracking and limits
Multiple clients: Concurrent trading clients
Thread safety: Atomic operations and concurrent collections
### 🧩 Scenario 24: Web Crawler with Rate Limiting
> **Problem Statement:** Build a multi-threaded web crawler with rate limiting, URL deduplication, and depth control.
> **Solution:**
```java
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;
import java.net.URL;

public class WebCrawlerWithRateLimiting {

    // URL information class
    static class UrlInfo {
        private final String url;
        private final int depth;
        private final long timestamp;

        public UrlInfo(String url, int depth) {
            this.url = url;
            this.depth = depth;
            this.timestamp = System.currentTimeMillis();
        }

        public String getUrl() { return url; }
        public int getDepth() { return depth; }
        public long getTimestamp() { return timestamp; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UrlInfo urlInfo = (UrlInfo) o;
            return url.equals(urlInfo.url);
        }

        @Override
        public int hashCode() {
            return url.hashCode();
        }
    }

    // Rate limiter using token bucket algorithm
    static class RateLimiter {
        private final int maxTokens;
        private final AtomicInteger availableTokens;
        private final long refillPeriod;
        private final AtomicLong lastRefillTime;

        public RateLimiter(int maxTokens, long refillPeriod, TimeUnit unit) {
            this.maxTokens = maxTokens;
            this.availableTokens = new AtomicInteger(maxTokens);
            this.refillPeriod = unit.toNanos(refillPeriod);
            this.lastRefillTime = new AtomicLong(System.nanoTime());
        }

        public boolean tryAcquire() {
            return tryAcquire(1);
        }

        public boolean tryAcquire(int tokens) {
            refillTokens();

            while (true) {
                int current = availableTokens.get();
                if (current >= tokens) {
                    if (availableTokens.compareAndSet(current, current - tokens)) {
                        return true;
                    }
                } else {
                    return false;
                }
            }
        }

        private void refillTokens() {
            long now = System.nanoTime();
            long lastRefill = lastRefillTime.get();
            long timePassed = now - lastRefill;

            if (timePassed >= refillPeriod) {
                if (lastRefillTime.compareAndSet(lastRefill, now)) {
                    int tokensToAdd = (int) (timePassed / refillPeriod);
                    int current = availableTokens.get();
                    int newTokens = Math.min(maxTokens, current + tokensToAdd);
                    availableTokens.set(newTokens);
                }
            }
        }

        public int getAvailableTokens() {
            refillTokens();
            return availableTokens.get();
        }
    }

    // Web crawler core
    static class WebCrawler implements Runnable {
        private final BlockingQueue<UrlInfo> urlQueue;
        private final Set<String> visitedUrls;
        private final RateLimiter rateLimiter;
        private final int maxDepth;
        private final AtomicInteger processedCount;
        private final AtomicInteger errorCount;
        private volatile boolean running = true;

        public WebCrawler(BlockingQueue<UrlInfo> urlQueue, Set<String> visitedUrls,
                         RateLimiter rateLimiter, int maxDepth) {
            this.urlQueue = urlQueue;
            this.visitedUrls = visitedUrls;
            this.rateLimiter = rateLimiter;
            this.maxDepth = maxDepth;
            this.processedCount = new AtomicInteger(0);
            this.errorCount = new AtomicInteger(0);
        }

        @Override
        public void run() {
            try {
                while (running) {
                    UrlInfo urlInfo = urlQueue.poll(1, TimeUnit.SECONDS);
                    if (urlInfo != null) {
                        crawlUrl(urlInfo);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void crawlUrl(UrlInfo urlInfo) {
            String url = urlInfo.getUrl();
            int depth = urlInfo.getDepth();

            // Check rate limit
            if (!rateLimiter.tryAcquire()) {
                System.out.println("Rate limit exceeded, re-queuing: " + url);
                try {
                    urlQueue.offer(urlInfo, 1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return;
            }

            // Check if already visited
            synchronized (visitedUrls) {
                if (visitedUrls.contains(url)) {
                    return;
                }
                visitedUrls.add(url);
            }

            try {
                // Simulate web crawling
                System.out.println("Crawling: " + url + " (depth: " + depth + ")");
                Thread.sleep(500 + (long) (Math.random() * 1000)); // Simulate network delay

                // Extract links (simulate)
                if (depth < maxDepth) {
                    List<String> extractedUrls = extractUrls(url);
                    for (String extractedUrl : extractedUrls) {
                        UrlInfo newUrlInfo = new UrlInfo(extractedUrl, depth + 1);
                        urlQueue.offer(newUrlInfo);
                    }
                }

                processedCount.incrementAndGet();
                System.out.println("Successfully crawled: " + url);

            } catch (Exception e) {
                errorCount.incrementAndGet();
                System.err.println("Error crawling " + url + ": " + e.getMessage());
            }
        }

        private List<String> extractUrls(String url) {
            // Simulate URL extraction
            List<String> urls = new ArrayList<>();
            int urlCount = 2 + (int) (Math.random() * 3);

            for (int i = 0; i < urlCount; i++) {
                urls.add(url + "/page" + i + ".html");
            }

            return urls;
        }

        public void stop() {
            running = false;
        }

        public int getProcessedCount() {
            return processedCount.get();
        }

        public int getErrorCount() {
            return errorCount.get();
        }
    }

    // URL validator
    static class UrlValidator {
        private static final Set<String> ALLOWED_DOMAINS = Set.of(
            "example.com", "test.com", "demo.com"
        );

        public static boolean isValidUrl(String url) {
            try {
                URL u = new URL(url);
                String host = u.getHost();
                return ALLOWED_DOMAINS.contains(host);
            } catch (Exception e) {
                return false;
            }
        }
    }

    // Statistics collector
    static class CrawlerStatistics implements Runnable {
        private final WebCrawler[] crawlers;
        private final BlockingQueue<UrlInfo> urlQueue;
        private final Set<String> visitedUrls;
        private volatile boolean running = true;

        public CrawlerStatistics(WebCrawler[] crawlers, BlockingQueue<UrlInfo> urlQueue,
                               Set<String> visitedUrls) {
            this.crawlers = crawlers;
            this.urlQueue = urlQueue;
            this.visitedUrls = visitedUrls;
        }

        @Override
        public void run() {
            try {
                while (running) {
                    Thread.sleep(5000); // Report every 5 seconds

                    int totalProcessed = 0;
                    int totalErrors = 0;
                    for (WebCrawler crawler : crawlers) {
                        totalProcessed += crawler.getProcessedCount();
                        totalErrors += crawler.getErrorCount();
                    }

                    System.out.println("\n=== Crawler Statistics ===");
                    System.out.println("Queue size: " + urlQueue.size());
                    System.out.println("Visited URLs: " + visitedUrls.size());
                    System.out.println("Total processed: " + totalProcessed);
                    System.out.println("Total errors: " + totalErrors);
                    System.out.println("Processing rate: " + (totalProcessed / 5) + " URLs/5sec");
                    System.out.println("========================\n");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void stop() {
            running = false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Configuration
        int maxDepth = 3;
        int crawlerThreads = 5;
        int rateLimit = 10; // URLs per second
        int maxQueueSize = 1000;

        // Create shared components
        BlockingQueue<UrlInfo> urlQueue = new LinkedBlockingQueue<>(maxQueueSize);
        Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
        RateLimiter rateLimiter = new RateLimiter(rateLimit, 1, TimeUnit.SECONDS);

        // Seed URLs
        List<String> seedUrls = Arrays.asList(
            "https://example.com",
            "https://test.com/index.html",
            "https://demo.com/home"
        );

        for (String seedUrl : seedUrls) {
            if (UrlValidator.isValidUrl(seedUrl)) {
                urlQueue.offer(new UrlInfo(seedUrl, 0));
            }
        }

        // Create and start crawlers
        WebCrawler[] crawlers = new WebCrawler[crawlerThreads];
        Thread[] crawlerThreadsArr = new Thread[crawlerThreads];

        for (int i = 0; i < crawlerThreads; i++) {
            crawlers[i] = new WebCrawler(urlQueue, visitedUrls, rateLimiter, maxDepth);
            crawlerThreadsArr[i] = new Thread(crawlers[i], "Crawler-" + (i + 1));
            crawlerThreadsArr[i].start();
        }

        // Start statistics collector
        CrawlerStatistics statistics = new CrawlerStatistics(crawlers, urlQueue, visitedUrls);
        Thread statsThread = new Thread(statistics, "Statistics");
        statsThread.start();

        // Let it run for a while
        Thread.sleep(30000); // Run for 30 seconds

        // Stop the system
        System.out.println("Stopping web crawler...");

        for (WebCrawler crawler : crawlers) {
            crawler.stop();
        }
        statistics.stop();

        // Wait for threads to complete
        for (Thread thread : crawlerThreadsArr) {
            thread.join();
        }
        statsThread.join();

        // Final statistics
        int totalProcessed = 0;
        int totalErrors = 0;
        for (WebCrawler crawler : crawlers) {
            totalProcessed += crawler.getProcessedCount();
            totalErrors += crawler.getErrorCount();
        }

        System.out.println("\n=== Final Statistics ===");
        System.out.println("Total URLs processed: " + totalProcessed);
        System.out.println("Total errors: " + totalErrors);
        System.out.println("Unique URLs visited: " + visitedUrls.size());
        System.out.println("Final queue size: " + urlQueue.size());

        System.out.println("Web crawler stopped");
    }
}
```
> **Explanation:** Web crawler demonstrates:
Rate limiting: Token bucket algorithm
URL deduplication: Concurrent set for visited URLs
Depth control: Prevents infinite crawling
Concurrent processing: Multiple crawler threads
Queue management: Bounded blocking queue
Statistics: Real-time progress monitoring

## 🚀 Modern Java Concurrency (Java 21+)

### 🧩 Scenario 26: Virtual Threads (Project Loom)
> **Problem Statement:** Create 100,000 threads to demonstrate the lightweight nature of Virtual Threads compared to Platform Threads.
> **Solution:**
```java
import java.util.concurrent.Executors;
import java.time.Duration;

public class VirtualThreadsDemo {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 100_000; i++) {
                executor.submit(() -> {
                    try {
                        Thread.sleep(Duration.ofMillis(100));
                    } catch (InterruptedException e) {
                        // ignore
                    }
                });
            }
        } // Executor closes and waits for all tasks here automatically
        
        long endTime = System.currentTimeMillis();
        System.out.println("Finished 100k threads in " + (endTime - startTime) + "ms");
    }
}
```
> **Explanation:** Virtual Threads are lightweight threads managed by the JVM, not the OS. You can create millions of them without running out of memory, making blocking I/O operations cheap.

### 🧩 Scenario 27: Structured Concurrency
> **Problem Statement:** Fetch data from two sources (User DB, Order DB) in parallel and fail if either fails, using Structured Concurrency.
> **Solution:**
```java
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class StructuredConcurrencyDemo {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            
            Supplier<String> userTask = scope.fork(() -> {
                Thread.sleep(100);
                return "User: John Doe";
            });
            
            Supplier<String> orderTask = scope.fork(() -> {
                Thread.sleep(200);
                return "Orders: [A, B, C]";
            });
            
            scope.join(); // Wait for both
            scope.throwIfFailed(); // Propagate exception if any failed
            
            System.out.println(userTask.get() + ", " + orderTask.get());
        }
    }
}
```
> **Explanation:** StructuredTasksScope ensures that related concurrent tasks are treated as a single unit of work, simplifying error handling and cancellation.

### 🧩 Scenario 28: StampedLock for Optimistic Reads
> **Problem Statement:** Implement a Point class with high-performance concurrent reads using StampedLock's optimistic locking.
> **Solution:**
```java
import java.util.concurrent.locks.StampedLock;

class Point {
    private double x, y;
    private final StampedLock sl = new StampedLock();

    void move(double deltaX, double deltaY) {
        long stamp = sl.writeLock();
        try {
            x += deltaX;
            y += deltaY;
        } finally {
            sl.unlockWrite(stamp);
        }
    }

    double distanceFromOrigin() {
        long stamp = sl.tryOptimisticRead();
        double currentX = x, currentY = y;
        
        if (!sl.validate(stamp)) { // Check if data changed
            stamp = sl.readLock(); // Fallback to read lock
            try {
                currentX = x;
                currentY = y;
            } finally {
                sl.unlockRead(stamp);
            }
        }
        return Math.sqrt(currentX * currentX + currentY * currentY);
    }
}
```
> **Explanation:** StampedLock allows "optimistic reads" which don't acquire a lock at all unless a modification happens during the read, significantly boosting performance for read-heavy workloads.

### 🧩 Scenario 29: Phaser for Dynamic Barriers
> **Problem Statement:** Coordinate a variable number of parties (threads) that can register and deregister dynamically during execution phases.
> **Solution:**
```java
import java.util.concurrent.Phaser;

public class PhaserDemo {
    public static void main(String[] args) {
        Phaser phaser = new Phaser(1); // Register main thread
        
        for (int i = 0; i < 3; i++) {
            phaser.register(); // Register new party
            new Thread(new Worker(phaser), "Worker-" + i).start();
        }
        
        System.out.println("Phase 0 starting");
        phaser.arriveAndAwaitAdvance(); // Wait for workers
        
        System.out.println("Phase 1 starting");
        phaser.arriveAndAwaitAdvance();
        
        phaser.arriveAndDeregister(); // Main thread done
    }
    
    static class Worker implements Runnable {
        private final Phaser phaser;
        Worker(Phaser phaser) { this.phaser = phaser; }
        
        public void run() {
            System.out.println(Thread.currentThread().getName() + " working phase 0");
            phaser.arriveAndAwaitAdvance();
            
            System.out.println(Thread.currentThread().getName() + " working phase 1");
            phaser.arriveAndAwaitAdvance();
        }
    }
}
```
> **Explanation:** Phaser is a improved CountDownLatch/CyclicBarrier that allows the number of registered parties to change over time.

### 🧩 Scenario 30: Scoped Values (Preview)
> **Problem Statement:** Share data (like a Transaction ID) safely within a request without using ThreadLocal to avoid memory leaks and inheritance issues.
> **Solution:**
```java
import java.util.concurrent.ScopedValue;

public class ScopedValueDemo {
    final static ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

    public static void main(String[] args) {
        ScopedValue.where(REQUEST_ID, "REQ-12345").run(() -> {
            processRequest();
        });
    }

    static void processRequest() {
        System.out.println("Processing " + REQUEST_ID.get());
        // No cleanup needed, automatically scoped!
    }
}
```
> **Explanation:** ScopedValue (Java 21 Preview) allows sharing immutable data for a bounded period of execution, solving many pitfalls of ThreadLocal.

## ✅ Best Practices & Anti-patterns
### 🧩 Scenario 25: Thread Safety Best Practices
> **Problem Statement:** Demonstrate common thread safety mistakes and their correct implementations.
> **Solution:**
```java
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;

public class ThreadSafetyBestPractices {

    // Anti-pattern 1: Mutable shared state without synchronization
    static class UnsafeCounter {
        private int count = 0;

        public void increment() {
            count++; // Race condition!
        }

        public int getCount() {
            return count; // May see stale value
        }
    }

    // Best practice 1: Use atomic variables
    static class SafeCounter {
        private final AtomicInteger count = new AtomicInteger(0);

        public void increment() {
            count.incrementAndGet();
        }

        public int getCount() {
            return count.get();
        }
    }

    // Anti-pattern 2: Check-then-act race condition
    static class UnsafeCheckThenAct {
        private int value = 0;

        public void checkAndSet(int newValue) {
            if (value == 0) { // Check
                try {
                    Thread.sleep(10); // Simulate other operations
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                value = newValue; // Act - race condition!
            }
        }

        public int getValue() {
            return value;
        }
    }

    // Best practice 2: Use synchronization or atomic operations
    static class SafeCheckThenAct {
        private final AtomicInteger value = new AtomicInteger(0);

        public void checkAndSet(int newValue) {
            value.compareAndSet(0, newValue); // Atomic check-then-act
        }

        public int getValue() {
            return value.get();
        }
    }

    // Anti-pattern 3: Non-thread-safe collections
    static class UnsafeCollection {
        private final List<String> items = new ArrayList<>(); // Not thread-safe!

        public void addItem(String item) {
            items.add(item); // Race condition!
        }

        public int getSize() {
            return items.size(); // May see inconsistent state
        }
    }

    // Best practice 3: Use concurrent collections
    static class SafeCollection {
        private final List<String> items = new CopyOnWriteArrayList<>();

        public void addItem(String item) {
            items.add(item);
        }

        public int getSize() {
            return items.size();
        }
    }

    // Anti-pattern 4: Improper synchronization
    static class ImproperSynchronization {
        private int counter = 0;
        private final Object lock1 = new Object();
        private final Object lock2 = new Object();

        public void increment() {
            synchronized (lock1) { // Different locks for read/write
                counter++;
            }
        }

        public int getCounter() {
            synchronized (lock2) { // Different lock - inconsistent!
                return counter;
            }
        }
    }

    // Best practice 4: Consistent synchronization
    static class ProperSynchronization {
        private int counter = 0;
        private final Object lock = new Object();

        public void increment() {
            synchronized (lock) {
                counter++;
            }
        }

        public int getCounter() {
            synchronized (lock) {
                return counter;
            }
        }
    }

    // Anti-pattern 5: Deadlock-prone nested locks
    static class DeadlockProne {
        private final Object lock1 = new Object();
        private final Object lock2 = new Object();

        public void method1() {
            synchronized (lock1) {
                synchronized (lock2) {
                    // Do something
                }
            }
        }

        public void method2() {
            synchronized (lock2) { // Different order!
                synchronized (lock1) {
                    // Do something
                }
            }
        }
    }

    // Best practice 5: Lock ordering
    static class DeadlockSafe {
        private final Object lock1 = new Object();
        private final Object lock2 = new Object();

        public void method1() {
            synchronized (lock1) {
                synchronized (lock2) {
                    // Do something
                }
            }
        }

        public void method2() {
            synchronized (lock1) { // Same order!
                synchronized (lock2) {
                    // Do something
                }
            }
        }
    }

    // Best practice 6: Use thread pools instead of creating threads
    static class ThreadPoolExample {
        private final ExecutorService executor = Executors.newFixedThreadPool(10);
        private final AtomicInteger taskCount = new AtomicInteger(0);

        public void submitTask(Runnable task) {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    taskCount.incrementAndGet();
                }
            });
        }

        public void shutdown() {
            executor.shutdown();
        }

        public int getTaskCount() {
            return taskCount.get();
        }
    }

    // Best practice 7: Use volatile for simple flags
    static class VolatileFlagExample {
        private volatile boolean running = true;

        public void stop() {
            running = false; // Visible to all threads immediately
        }

        public void doWork() {
            while (running) {
                // Do work
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    // Best practice 8: Immutability
    static final class ImmutableData {
        private final int value;
        private final String name;
        private final List<String> items;

        public ImmutableData(int value, String name, List<String> items) {
            this.value = value;
            this.name = name;
            this.items = Collections.unmodifiableList(new ArrayList<>(items));
        }

        public int getValue() { return value; }
        public String getName() { return name; }
        public List<String> getItems() { return items; }

        // No setters - immutable!
    }

    // Demonstration
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Demonstrating Thread Safety Issues ===");

        // Demonstrate unsafe counter
        System.out.println("\n1. Unsafe Counter:");
        UnsafeCounter unsafeCounter = new UnsafeCounter();
        Thread[] threads = new Thread[10];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    unsafeCounter.increment();
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Unsafe counter result: " + unsafeCounter.getCount() + " (should be 10000)");

        // Demonstrate safe counter
        System.out.println("\n2. Safe Counter:");
        SafeCounter safeCounter = new SafeCounter();

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    safeCounter.increment();
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Safe counter result: " + safeCounter.getCount() + " (should be 10000)");

        // Demonstrate thread pool
        System.out.println("\n3. Thread Pool Example:");
        ThreadPoolExample pool = new ThreadPoolExample();

        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            pool.submitTask(() -> {
                System.out.println("Task " + taskId + " executed by " + Thread.currentThread().getName());
            });
        }

        Thread.sleep(2000);
        pool.shutdown();
        System.out.println("Total tasks executed: " + pool.getTaskCount());

        System.out.println("\n=== Best Practices Summary ===");
        System.out.println("1. Use atomic variables for simple counters");
        System.out.println("2. Use concurrent collections for shared data structures");
        System.out.println("3. Use consistent lock ordering to prevent deadlocks");
        System.out.println("4. Use thread pools instead of creating threads manually");
        System.out.println("5. Use volatile for simple flags that need visibility");
        System.out.println("6. Prefer immutability when possible");
        System.out.println("7. Always use try-finally with locks");
        System.out.println("8. Avoid nested locks when possible");
    }
}
```
> **Explanation:** Thread safety best practices:
Atomic variables: For simple counters and flags
Concurrent collections: For shared data structures
Consistent locking: Same lock order to prevent deadlocks
Thread pools: Better resource management
Volatile: For visibility of simple variables
Immutability: Thread-safe by design
Try-finally: Always release locks in finally block
This comprehensive tutorial covers 100+ real-world Java threading scenarios with detailed explanations. Each scenario includes:
> **Problem Statement:** Clear description of the real-world problem
> **Solution:**
> **Explanation:** Detailed explanation of the threading concepts used
Best Practices: Industry-standard approaches to common problems
The scenarios progress from basic threading concepts to advanced concurrent programming patterns, providing a complete learning path for Java multithreading mastery.

# 🧵 Section 3: Java Multithreading & Concurrency (100 Scenarios)

### Thread Lifecycle & Basic Management
1.  **Create Thread via Runnable**: `new Thread(() -> System.out.println("Running")).start()`
2.  **Create Thread by subclassing**: `class MyThread extends Thread { @Override public void run() { ... } }`
3.  **Wait for Thread to finish**: `thread.join()`
4.  **Pause execution (Interruptible)**: `Thread.sleep(1000)`
5.  **Give up CPU hint**: `Thread.yield()`
6.  **Check if Thread is alive**: `thread.isAlive()`
7.  **Set Thread priority**: `thread.setPriority(Thread.MAX_PRIORITY)`
8.  **Daemon Thread (Background task)**: `thread.setDaemon(true)`
9.  **Interrupt a Thread**: `thread.interrupt()`
10. **Check interruption status**: `Thread.currentThread().isInterrupted()`
11. **Get current Thread reference**: `Thread.currentThread()`
12. **Handle Uncaught Exceptions**: `thread.setUncaughtExceptionHandler((t, e) -> ...)`
13. **Name a Thread for debugging**: `thread.setName("Order-Processor-01")`
14. **Wait/Notify (Object Monitor)**: `synchronized(lock) { lock.wait(); }`
15. **Notify all waiting threads**: `synchronized(lock) { lock.notifyAll(); }`



### Executor Framework & Thread Pools
16. **Fixed Thread Pool (Reusing threads)**: `Executors.newFixedThreadPool(10)`
17. **Cached Thread Pool (Elastic size)**: `Executors.newCachedThreadPool()`
18. **Single Thread Executor (Sequential)**: `Executors.newSingleThreadExecutor()`
19. **Scheduled Task (Fixed Rate)**: `Executors.newScheduledThreadPool(1).scheduleAtFixedRate(...)`
20. **Submit Task with return value**: `Future<String> future = executor.submit(() -> "Done")`
21. **Check if task is complete**: `future.isDone()`
22. **Blocking get on Future**: `future.get(5, TimeUnit.SECONDS)`
23. **Shutdown Executor gracefully**: `executor.shutdown(); executor.awaitTermination(...)`
24. **Immediate Executor halt**: `executor.shutdownNow()`
25. **Custom ThreadFactory**: `Executors.newFixedThreadPool(n, r -> new Thread(r, "Name"))`
26. **ForkJoinPool (Parallelism)**: `ForkJoinPool.commonPool().submit(recursiveTask)`
27. **CompletableFuture (Async chaining)**: `CompletableFuture.supplyAsync(s).thenApply(f).thenAccept(c)`
28. **Handle Future Exception**: `future.exceptionally(ex -> "Default Value")`
29. **Wait for multiple Futures**: `CompletableFuture.allOf(f1, f2).join()`
30. **Work-Stealing Pool**: `Executors.newWorkStealingPool()`



### Synchronization & Locking
31. **Synchronized Method**: `public synchronized void increment() { ... }`
32. **Synchronized Block (Fine-grained)**: `synchronized(this) { criticalSection(); }`
33. **ReentrantLock (Explicit)**: `lock.lock(); try { ... } finally { lock.unlock(); }`
34. **Try-Lock (Non-blocking)**: `if (lock.tryLock()) { ... }`
35. **Read-Write Lock (High Read)**: `readWriteLock.readLock().lock()`
36. **Optimistic Locking**: `StampedLock lock = new StampedLock(); long stamp = lock.tryOptimisticRead()`
37. **Condition Variables**: `condition.await(); condition.signalAll();`
38. **Atomic Counter**: `new AtomicInteger(0).incrementAndGet()`
39. **Compare and Swap (CAS)**: `atomicRef.compareAndSet(expected, newValue)`
40. **Thread-local storage**: `ThreadLocal<UserSession> session = new ThreadLocal<>()`

### Synchronization Aids (Barriers & Latches)
41. **Wait for N events (One-time)**: `CountDownLatch latch = new CountDownLatch(3); latch.countDown();`
42. **Cyclic Barrier (Reusable)**: `new CyclicBarrier(4, () -> System.out.println("Done"))`
43. **Semaphore (Throttling)**: `Semaphore limit = new Semaphore(5); limit.acquire();`
44. **Exchanger (Data swap)**: `exchanger.exchange(myData)`
45. **Phaser (Multi-phase sync)**: `phaser.register(); phaser.arriveAndAwaitAdvance()`



---

## 💻 Section 4: Threading Coding Interview Scenarios

### 81. The "Producer-Consumer" Scenario
**Task:** Coordinate a producer thread adding items to a queue and a consumer thread removing them.
* **Correct Choice:** `ArrayBlockingQueue`. It handles internal `wait/notify` logic and capacity limits automatically.

### 82. The "Sequence Generator" (1, 2, 3 in order)
**Task:** Thread A prints "1", B prints "2", C prints "3" using 3 separate threads in a loop.
* **Correct Choice:** Use three `Semaphores` (s1, s2, s3). Thread A acquires s1 and releases s2; B acquires s2 and releases s3, and so on.

### 83. The "Deadlock" Prevention
**Task:** Two threads need Lock A and Lock B. How do you prevent a deadlock?
* **Correct Choice:** **Lock Ordering**. Ensure every thread in the system always acquires Lock A before Lock B.

### 84. The "Thread-Safe Singleton"
**Task:** Ensure only one instance of a class is created in a multi-threaded environment.
* **Correct Choice:** **Double-checked locking** using the `volatile` keyword to prevent instruction reordering.

### 85. The "Rate Limiter"
**Task:** Allow only 100 API calls per second across multiple threads.
* **Correct Choice:** `Semaphore(100)` with a background scheduled thread that releases all permits once per second.
