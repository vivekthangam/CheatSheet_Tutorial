package com.cheatsheet.webflux.designlab;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Architectural Anti-Patterns & Refactorings (DesignLab)
 */
public class WebFluxDesignLab {

    public record MarketTick(String symbol, BigDecimal price) {}

    // =========================================================================
    // ANTI-PATTERN 1: Blocking JDBC Calls Inside WebFlux Pipelines
    // =========================================================================

    public interface BlockingTicketService {
        MarketTick findById(Long id);
    }

    public static class BlockingJdbcPipelineTrap {
        private final BlockingTicketService blockingService;

        public BlockingJdbcPipelineTrap(BlockingTicketService blockingService) {
            this.blockingService = blockingService;
        }

        // ⚠️ ANTI-PATTERN: Calling blocking service directly on Netty EventLoop
        public Mono<MarketTick> getTickVulnerable(Long id) {
            return Mono.justOrEmpty(blockingService.findById(id));
        }

        // 🏗️ REFACTOR: Offload blocking I/O to boundedElastic scheduler pool
        public Mono<MarketTick> getTickHardened(Long id) {
            return Mono.fromCallable(() -> blockingService.findById(id))
                    .subscribeOn(Schedulers.boundedElastic());
        }
    }

    // =========================================================================
    // ANTI-PATTERN 2: Leaking Cold Publishers for Multicast Streams
    // =========================================================================

    @Service
    public static class MulticastStreamingDesign {

        // ⚠️ ANTI-PATTERN: Cold Flux recreates connection for every single client
        public Flux<MarketTick> getColdFeed() {
            return Flux.interval(Duration.ofMillis(100))
                    .map(i -> new MarketTick("BTC-USD", BigDecimal.valueOf(50000.0 + i)));
        }

        // 🏗️ REFACTOR: Use .share() to convert to a hot multicasting stream
        private final Flux<MarketTick> hotMulticastFeed = Flux.interval(Duration.ofMillis(100))
                .map(i -> new MarketTick("BTC-USD", BigDecimal.valueOf(50000.0 + i)))
                .share();

        public Flux<MarketTick> getHotFeed() {
            return hotMulticastFeed;
        }
    }
}
