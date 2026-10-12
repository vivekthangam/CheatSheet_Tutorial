package com.cheatsheet.security.buglab;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Real-World Security Outage Bugs & Refactorings (BugLab)
 */
public class SecurityBugLab {

    // =========================================================================
    // BUG 1: Missing "ROLE_" Prefix in Authority Converter Causing 403 Lockout
    // =========================================================================

    // ❌ BUG: Converter creates authorities without "ROLE_" prefix; hasRole("ADMIN") permanently fails
    public static class FlawedJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {
        @Override
        public AbstractAuthenticationToken convert(Jwt jwt) {
            List<String> roles = jwt.getClaimAsStringList("roles");
            var authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();
            return new JwtAuthenticationToken(jwt, authorities);
        }
    }

    // ✅ REFACTORED: Normalize roles with standard "ROLE_" prefix in converter
    public static class HardenedJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {
        @Override
        public AbstractAuthenticationToken convert(Jwt jwt) {
            List<String> roles = jwt.getClaimAsStringList("roles");
            var authorities = roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
            return new JwtAuthenticationToken(jwt, authorities);
        }
    }

    // =========================================================================
    // BUG 2: SecurityContext Lost Across @Async Worker Thread Boundaries
    // =========================================================================

    // ❌ BUG: Default ThreadLocal SecurityContext does NOT propagate into @Async worker threads
    @Service
    public static class FlawedAuditService {
        @Async
        public void recordTransferAudit(String transferId) {
            // 💥 Throws NullPointerException under worker pool because SecurityContext is empty!
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                String operator = auth.getName();
                System.out.println("Auditing transfer " + transferId + " by " + operator);
            }
        }
    }

    // ✅ REFACTORED: Configure DelegatingSecurityContextAsyncTaskExecutor for async propagation
    @Configuration
    public static class HardenedAsyncSecurityConfig {
        @Bean
        public AsyncTaskExecutor taskExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setCorePoolSize(10);
            executor.setMaxPoolSize(50);
            executor.setThreadNamePrefix("async-sec-");
            executor.initialize();
            return new DelegatingSecurityContextAsyncTaskExecutor(executor);
        }
    }

    // =========================================================================
    // BUG 3: Unhandled Custom Pre-Filter Failing permitAll() Health Probes
    // =========================================================================

    // ❌ BUG: Custom filter throws exception before permitAll() authorization filter is reached
    public static class FlawedApiKeyFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws ServletException, IOException {
            String key = req.getHeader("X-Api-Key");
            if (key == null) {
                // 💥 Throws on all requests, crashing /actuator/health Kubernetes readiness probes!
                throw new BadCredentialsException("Missing API key header");
            }
            chain.doFilter(req, res);
        }
    }

    // ✅ REFACTORED: Bypass filter logic for permitAll paths or delegate to filterChain
    public static class HardenedApiKeyFilter extends OncePerRequestFilter {
        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String path = request.getRequestURI();
            return path.startsWith("/actuator/health") || path.startsWith("/api/v1/public");
        }

        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws ServletException, IOException {
            String key = req.getHeader("X-Api-Key");
            if (key != null && key.equals("secret-trusted-key")) {
                // Set authenticated API key identity
            }
            chain.doFilter(req, res);
        }
    }
}
