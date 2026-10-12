package com.cheatsheet.webflux.buglab;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Real-World Reactive Outage Bugs & Fixes (BugLab)
 */
public class WebFluxBugLab {

    public record MarketTick(String symbol, BigDecimal price) {}
    public record Config(String apiKey) {}
    public record AuditLog(String action, String traceId, Instant timestamp) {}

    // =========================================================================
    // BUG 1: Calling .block() on EventLoop Thread Freezing Application
    // =========================================================================

    public static class EventLoopBlockingBug {
        private final WebClient client = WebClient.create();

        // ❌ BUG: Calling .block() on Netty EventLoop worker thread freezes the server
        public Mono<BigDecimal> fetchPriceBlocking(String symbol) {
            return client.get().uri("/ticks/" + symbol).retrieve().bodyToMono(MarketTick.class)
                    .flatMap(tick -> {
                        // 💥 FATAL: Blocks the Netty EventLoop worker thread!
                        Config config = client.get().uri("/config").retrieve().bodyToMono(Config.class).block();
                        return Mono.just(tick.price());
                    });
        }

        // ✅ REFACTORED: Compose reactively with flatMap without ever blocking
        public Mono<BigDecimal> fetchPriceNonBlocking(String symbol) {
            return client.get().uri("/ticks/" + symbol).retrieve().bodyToMono(MarketTick.class)
                    .flatMap(tick -> client.get().uri("/config").retrieve().bodyToMono(Config.class)
                            .map(config -> tick.price()));
        }
    }

    // =========================================================================
    // BUG 2: Missing Backpressure Buffer Strategy Causing OverflowException
    // =========================================================================

    public static class BackpressureOverflowBug {
        // ❌ BUG: Unbounded default buffer throws OverflowException when subscriber is slow
        public Flux<MarketTick> ingestUnbounded(Flux<MarketTick> upstreamTicks) {
            return upstreamTicks
                    .publishOn(Schedulers.parallel()); // Default 256 queue overflows quickly
        }

        // ✅ REFACTORED: Explicit backpressure buffer with bounded size and DROP_OLDEST strategy
        public Flux<MarketTick> ingestHardened(Flux<MarketTick> upstreamTicks) {
            return upstreamTicks
                    .onBackpressureBuffer(
                            10_000,
                            dropped -> System.err.println("Dropped stale quote for " + dropped.symbol()),
                            BufferOverflowStrategy.DROP_OLDEST
                    )
                    .publishOn(Schedulers.parallel(), 1024);
        }
    }

    // =========================================================================
    // BUG 3: Assembly Time Evaluation Trap
    // =========================================================================

    public static class AssemblyTimeTrapBug {
        // ❌ BUG: Assembly Time evaluation — Instant.now() is calculated once when pipeline is assembled
        public Mono<AuditLog> createAuditLogStale(String action) {
            return Mono.just(new AuditLog(action, UUID.randomUUID().toString(), Instant.now()));
        }

        // ✅ REFACTORED: Mono.defer() defers evaluation until subscription time per request
        public Mono<AuditLog> createAuditLogDynamic(String action) {
            return Mono.defer(() -> Mono.just(new AuditLog(action, UUID.randomUUID().toString(), Instant.now())));
        }
    }
}
