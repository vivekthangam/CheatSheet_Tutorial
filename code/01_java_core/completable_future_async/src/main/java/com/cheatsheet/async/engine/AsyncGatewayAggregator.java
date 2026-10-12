package com.cheatsheet.async.engine;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/**
 * Resilient Async API Gateway Aggregator Engine
 * Demonstrates non-blocking fan-out / fan-in DAG pipelines, timeout boundaries, and fallback recovery.
 */
@Service
public class AsyncGatewayAggregator implements AutoCloseable {

    private final ExecutorService ioThreadPool;

    public AsyncGatewayAggregator() {
        this(8, 16, 500);
    }

    public AsyncGatewayAggregator(int corePoolSize, int maxPoolSize, int queueCapacity) {
        // Dedicated, bounded thread pool preventing commonPool starvation
        this.ioThreadPool = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                new ThreadFactory() {
                    private int counter = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "async-gateway-io-" + (++counter));
                        t.setDaemon(true);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // Backpressure protection
        );
    }

    public record UserProfile(String userId, String username, String email) {}
    public record OrderHistory(String userId, List<String> orderIds) {}
    public record CreditScore(String userId, int score) {}
    public record AggregatedAccountDetails(UserProfile profile, OrderHistory orders, CreditScore credit) {}

    // ========================================================================
    // NON-BLOCKING FAN-OUT / FAN-IN PIPELINE WITH PER-STAGE TIMEOUTS
    // ========================================================================
    public CompletableFuture<AggregatedAccountDetails> aggregateAccountDetails(
            String userId,
            Duration timeoutSla
    ) {
        // Stage 1: Async fetch UserProfile with fallback on failure
        CompletableFuture<UserProfile> profileFuture = CompletableFuture
                .supplyAsync(() -> fetchUserProfileRemote(userId), ioThreadPool)
                .orTimeout(timeoutSla.toMillis(), TimeUnit.MILLISECONDS)
                .exceptionally(ex -> new UserProfile(userId, "Anonymous Guest", "guest@system.local"));

        // Stage 2: Async fetch OrderHistory with fallback on timeout
        CompletableFuture<OrderHistory> ordersFuture = CompletableFuture
                .supplyAsync(() -> fetchOrderHistoryRemote(userId), ioThreadPool)
                .completeOnTimeout(new OrderHistory(userId, List.of()), timeoutSla.toMillis(), TimeUnit.MILLISECONDS);

        // Stage 3: Async fetch CreditScore
        CompletableFuture<CreditScore> creditFuture = CompletableFuture
                .supplyAsync(() -> fetchCreditScoreRemote(userId), ioThreadPool)
                .orTimeout(timeoutSla.toMillis(), TimeUnit.MILLISECONDS)
                .exceptionally(ex -> new CreditScore(userId, 600)); // Default fallback score

        // Barrier Fan-In: Combine all 3 futures without blocking worker threads
        return CompletableFuture.allOf(profileFuture, ordersFuture, creditFuture)
                .thenApply(v -> new AggregatedAccountDetails(
                        profileFuture.join(),
                        ordersFuture.join(),
                        creditFuture.join()
                ));
    }

    // ========================================================================
    // MONADIC ASYNC ERROR RECOVERY WITH exceptionallyCompose
    // ========================================================================
    public CompletableFuture<String> fetchWithSecondaryFailover(String primaryUrl, String secondaryUrl) {
        return CompletableFuture.supplyAsync(() -> callHttpEndpoint(primaryUrl), ioThreadPool)
                .exceptionallyCompose(ex -> {
                    // Transparent asynchronous failover to secondary replica
                    return CompletableFuture.supplyAsync(() -> callHttpEndpoint(secondaryUrl), ioThreadPool);
                });
    }

    public ExecutorService getIoThreadPool() {
        return ioThreadPool;
    }

    // Mock remote I/O methods
    protected UserProfile fetchUserProfileRemote(String userId) {
        return new UserProfile(userId, "JaneDoe", "jane.doe@enterprise.com");
    }

    protected OrderHistory fetchOrderHistoryRemote(String userId) {
        return new OrderHistory(userId, List.of("ORD-901", "ORD-902"));
    }

    protected CreditScore fetchCreditScoreRemote(String userId) {
        return new CreditScore(userId, 785);
    }

    protected String callHttpEndpoint(String endpoint) {
        if (endpoint.contains("fail")) throw new RuntimeException("HTTP 500 from " + endpoint);
        return "SUCCESS from " + endpoint;
    }

    @Override
    public void close() {
        ioThreadPool.shutdown();
    }
}
