package com.cheatsheet.security.designlab;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Architectural Anti-Patterns & Refactorings (DesignLab)
 */
public class SecurityDesignLab {

    // =========================================================================
    // ANTI-PATTERN 1: Monolithic Security Filter God-Class
    // =========================================================================

    // ⚠️ ANTI-PATTERN: Monolithic Filter mixing IP checks, rate limits, JWT logic, and audits
    public static class OmniSecurityFilter extends GenericFilterBean {
        @Override
        public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
                throws IOException, ServletException {
            // Un-testable monolithic spaghetti code combining 5 separate security concerns
            chain.doFilter(req, res);
        }
    }

    public static class RateLimitingFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
                                        jakarta.servlet.http.HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            // Single responsibility: token bucket rate limiting
            filterChain.doFilter(request, response);
        }
    }

    // 🏗️ REFACTOR: Decomposed modular filter chain with standard Spring Security filters
    @Configuration
    public static class ModularSecurityConfig {
        @Bean
        public SecurityFilterChain modularFilterChain(HttpSecurity http) throws Exception {
            return http
                .addFilterBefore(new RateLimitingFilter(), BearerTokenAuthenticationFilter.class)
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
                .build();
        }
    }

    // =========================================================================
    // ANTI-PATTERN 2: Synchronous Remote Token Introspection Bottleneck
    // =========================================================================

    @Configuration
    public static class TokenValidationArchitecture {
        // ⚠️ ANTI-PATTERN: Remote opaque token introspection creates a 150ms HTTP latency bottleneck per request
        public OpaqueTokenIntrospector slowIntrospector() {
            return new SpringOpaqueTokenIntrospector("https://idp.bank/oauth2/introspect", "client-id", "secret");
        }

        // 🏗️ REFACTOR: Stateless Cryptographic JWT Validation with Local In-Memory JWKS Caching (< 0.05ms)
        @Bean
        public JwtDecoder localCachedJwtDecoder() {
            return NimbusJwtDecoder.withJwkSetUri("https://idp.bank/.well-known/jwks.json")
                .cache(new ConcurrentMapCache("jwksCache"))
                .build();
        }
    }
}
