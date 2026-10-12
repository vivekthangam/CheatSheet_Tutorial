package com.cheatsheet.webflux.engine;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class MarketDataIngestionEngine {

    // ============================================================================
    // 1. REACTIVE DOMAIN MODELS & R2DBC ENTITY
    // ============================================================================
    @Table("market_ticks")
    public record MarketTick(
        @Id Long id,
        String symbol,
        BigDecimal bidPrice,
        BigDecimal askPrice,
        BigDecimal volume,
        Instant timestamp
    ) {
        public static MarketTick of(String symbol, BigDecimal bid, BigDecimal ask, BigDecimal vol) {
            return new MarketTick(null, symbol, bid, ask, vol, Instant.now());
        }
    }

    public record MarketSummary(String symbol, BigDecimal averagePrice, long tickCount, Instant windowEnd) {}

    // ============================================================================
    // 2. R2DBC REACTIVE REPOSITORY
    // ============================================================================
    @Repository
    public interface ReactiveMarketTickRepository extends ReactiveCrudRepository<MarketTick, Long> {
        Flux<MarketTick> findBySymbolOrderByTimestampDesc(String symbol);
    }

    // ============================================================================
    // 3. NETTY-TUNED HIGH-THROUGHPUT WEBCLIENT CONFIGURATION
    // ============================================================================
    @Configuration
    public static class ReactiveNettyClientConfig {

        @Bean
        public WebClient marketFeedWebClient() {
            ConnectionProvider provider = ConnectionProvider.builder("market-feed-pool")
                    .maxConnections(500)
                    .pendingAcquireMaxCount(2000)
                    .pendingAcquireTimeout(Duration.ofSeconds(5))
                    .maxIdleTime(Duration.ofSeconds(30))
                    .maxLifeTime(Duration.ofMinutes(5))
                    .evictInBackground(Duration.ofSeconds(60))
                    .build();

            HttpClient httpClient = HttpClient.create(provider)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .doOnConnected(conn -> conn
                            .addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)))
                    .compress(true);

            return WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .codecs(config -> config.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                    .build();
        }
    }

    // ============================================================================
    // 4. REACTIVE STREAM INGESTION & BROADCAST SERVICE
    // ============================================================================
    @Service
    public static class MarketDataStreamingService {

        private final WebClient webClient;
        private final ReactiveMarketTickRepository repository;
        private final DatabaseClient databaseClient;
        
        private final Sinks.Many<MarketTick> tickBroadcastSink = 
                Sinks.many().multicast().onBackpressureBuffer(10_000, false);

        public MarketDataStreamingService(WebClient webClient, 
                                         ReactiveMarketTickRepository repository,
                                         DatabaseClient databaseClient) {
            this.webClient = webClient;
            this.repository = repository;
            this.databaseClient = databaseClient;
        }

        public Flux<MarketTick> startMarketFeedIngestion(String symbol) {
            return Flux.interval(Duration.ofMillis(50))
                    .onBackpressureDrop(droppedIndex -> 
                        System.err.println("Slow consumer dropped tick sequence: " + droppedIndex))
                    .map(i -> MarketTick.of(
                            symbol,
                            BigDecimal.valueOf(50000.00 + (Math.random() * 20.0 - 10.0)),
                            BigDecimal.valueOf(50001.00 + (Math.random() * 20.0 - 10.0)),
                            BigDecimal.valueOf(Math.random() * 5.0)
                    ))
                    .publishOn(Schedulers.parallel())
                    .onBackpressureBuffer(
                            5000,
                            dropped -> System.err.println("Buffer overflow! Dropping tick for: " + dropped.symbol()),
                            BufferOverflowStrategy.DROP_OLDEST
                    )
                    .flatMap(tick -> repository.save(tick).subscribeOn(Schedulers.boundedElastic()), 64)
                    .doOnNext(savedTick -> tickBroadcastSink.tryEmitNext(savedTick))
                    .retryWhen(Retry.backoff(10, Duration.ofMillis(100))
                            .maxBackoff(Duration.ofSeconds(2))
                            .jitter(0.5)
                            .doBeforeRetry(retrySignal -> 
                                System.out.println("Transient feed error, retrying attempt #" + retrySignal.totalRetries())))
                    .share();
        }

        public Flux<MarketTick> getLiveBroadcastingFeed(String symbol) {
            return tickBroadcastSink.asFlux()
                    .filter(tick -> tick.symbol().equalsIgnoreCase(symbol));
        }
    }

    // ============================================================================
    // 5. SERVER-SENT EVENTS (SSE) & REACTIVE CONTROLLER
    // ============================================================================
    @RestController
    @RequestMapping("/api/v1/market")
    public static class MarketDataReactiveController {

        private final MarketDataStreamingService streamingService;

        public MarketDataReactiveController(MarketDataStreamingService streamingService) {
            this.streamingService = streamingService;
        }

        @GetMapping(value = "/feed/{symbol}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        public Flux<MarketTick> streamMarketTicks(@PathVariable String symbol) {
            return streamingService.getLiveBroadcastingFeed(symbol)
                    .mergeWith(streamingService.startMarketFeedIngestion(symbol))
                    .sample(Duration.ofMillis(100));
        }

        @GetMapping(value = "/summary/{symbol}", produces = MediaType.APPLICATION_JSON_VALUE)
        public Mono<MarketSummary> getWindowSummary(@PathVariable String symbol) {
            return streamingService.getLiveBroadcastingFeed(symbol)
                    .take(Duration.ofSeconds(5))
                    .collectList()
                    .map(ticks -> {
                        BigDecimal avg = ticks.isEmpty() ? BigDecimal.ZERO :
                                BigDecimal.valueOf(ticks.stream()
                                        .mapToDouble(t -> t.bidPrice().doubleValue())
                                        .average().orElse(0.0));
                        return new MarketSummary(symbol, avg, ticks.size(), Instant.now());
                    });
        }
    }
}
