package com.cheatsheet.webflux.securitylab;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Real-World Security Vulnerabilities & Mitigations (SecurityLab)
 */
public class WebFluxSecurityLab {

    // =========================================================================
    // VULNERABILITY 1: Unbounded In-Memory WebClient Buffer (DoS Attack)
    // =========================================================================

    @Configuration
    public static class WebClientBufferDefense {
        // 🚨 VULNERABILITY: Unlimited buffer allows attacker to exhaust JVM heap
        public WebClient vulnerableWebClient() {
            return WebClient.builder()
                    .codecs(config -> config.defaultCodecs().maxInMemorySize(-1)) // 💥 Unlimited memory allocation!
                    .build();
        }

        // 🛡️ MITIGATION: Strictly cap maxInMemorySize to prevent heap exhaustion
        @Bean
        public WebClient secureWebClient() {
            return WebClient.builder()
                    .codecs(config -> config.defaultCodecs().maxInMemorySize(256 * 1024)) // 256 KB limit
                    .build();
        }
    }

    // =========================================================================
    // VULNERABILITY 2: Reactive SecurityContext Loss Across Thread Transitions
    // =========================================================================

    @Configuration
    public static class ReactiveSecurityContextBridge {
        // 🚨 VULNERABILITY: Thread switching clears ThreadLocal security, causing authorization bypass
        public Mono<Void> executeTradeVulnerable(String orderId) {
            return Mono.just(orderId)
                    .publishOn(Schedulers.parallel()) // 💥 Loses ThreadLocal SecurityContext!
                    .flatMap(id -> Mono.empty());
        }

        // 🛡️ MITIGATION: Enable automatic context propagation across Project Reactor schedulers
        @PostConstruct
        public void setupContextPropagation() {
            Hooks.enableAutomaticContextPropagation();
        }
    }
}
