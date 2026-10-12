package com.cheatsheet.security.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// ============================================================================
// 1. DOMAIN MODELS & FINANCIAL TRANSFER DTOs
// ============================================================================
public class FintechAuthGatewayEngine {

    public record TransferRequest(String sourceAccountId, String targetAccountId, BigDecimal amount, String currency) {}
    public record TransferResponse(String transactionId, String status, Instant timestamp) {}
    public record SecurityErrorResponse(String type, String title, int status, String detail, Instant timestamp) {}

    // ============================================================================
    // 2. DISTRIBUTED TOKEN BLACKLIST SERVICE (Fast-Revocation Defense)
    // ============================================================================
    public interface TokenRevocationRegistry {
        void revokeToken(String jti, Instant expiresAt);
        boolean isRevoked(String jti);
    }

    @org.springframework.stereotype.Service
    public static class DistributedTokenRevocationRegistry implements TokenRevocationRegistry {
        private final Map<String, Instant> revokedJtis = new ConcurrentHashMap<>();

        @Override
        public void revokeToken(String jti, Instant expiresAt) {
            revokedJtis.put(jti, expiresAt);
        }

        @Override
        public boolean isRevoked(String jti) {
            Instant exp = revokedJtis.get(jti);
            if (exp == null) return false;
            if (Instant.now().isAfter(exp)) {
                revokedJtis.remove(jti);
                return false;
            }
            return true;
        }
    }

    // ============================================================================
    // 3. ZERO-TRUST JWT REVOCATION FILTER
    // ============================================================================
    public static class JwtRevocationCheckFilter extends OncePerRequestFilter {
        private final TokenRevocationRegistry revocationRegistry;

        public JwtRevocationCheckFilter(TokenRevocationRegistry revocationRegistry) {
            this.revocationRegistry = revocationRegistry;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth instanceof JwtAuthenticationToken jwtAuth) {
                    String jti = jwtAuth.getToken().getId();
                    if (jti != null && revocationRegistry.isRevoked(jti)) {
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.getWriter().write("""
                            {"error": "invalid_token", "message": "Access token has been revoked by security policy"}
                        """);
                        return;
                    }
                }
            }
            filterChain.doFilter(request, response);
        }
    }

    // ============================================================================
    // 4. FINE-GRAINED JWT CONVERTER (Extracts Roles, Scopes, and Tenant Bounds)
    // ============================================================================
    public static class FintechJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
        @Override
        public AbstractAuthenticationToken convert(Jwt jwt) {
            Set<GrantedAuthority> authorities = new HashSet<>();

            List<String> scopes = jwt.getClaimAsStringList("scope");
            if (scopes != null) {
                scopes.stream()
                      .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                      .forEach(authorities::add);
            }

            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.stream()
                     .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                     .map(SimpleGrantedAuthority::new)
                     .forEach(authorities::add);
            }

            String tenantId = jwt.getClaimAsString("tenant_id");
            if (tenantId != null) {
                authorities.add(new SimpleGrantedAuthority("TENANT_" + tenantId));
            }

            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        }
    }

    // ============================================================================
    // 5. SECURITY FILTER CHAIN & CONFIGURATION
    // ============================================================================
    @Configuration
    @EnableWebSecurity
    @EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
    public static class FintechSecurityConfig {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Bean
        public SecurityFilterChain securityFilterChain(
                HttpSecurity http,
                TokenRevocationRegistry revocationRegistry,
                JwtDecoder jwtDecoder) throws Exception {

            http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(customAuthenticationEntryPoint())
                    .accessDeniedHandler(customAccessDeniedHandler())
                )
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/tokens").permitAll()
                    .requestMatchers("/api/v1/admin/**").hasRole("SECURITY_ADMIN")
                    .requestMatchers("/api/v1/transfers/**").hasAuthority("SCOPE_transfers:execute")
                    .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt
                        .decoder(jwtDecoder)
                        .jwtAuthenticationConverter(new FintechJwtAuthenticationConverter())
                    )
                )
                .addFilterAfter(new JwtRevocationCheckFilter(revocationRegistry), UsernamePasswordAuthenticationFilter.class);

            return http.build();
        }

        @Bean
        public JwtDecoder jwtDecoder() {
            NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri("https://auth.fintech.bank/.well-known/jwks.json").build();
            OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer("https://auth.fintech.bank");
            OAuth2TokenValidator<Jwt> withAudience = new OAuth2TokenValidator<>() {
                @Override
                public OAuth2TokenValidatorResult validate(Jwt token) {
                    if (token.getAudience().contains("fintech-gateway-api")) {
                        return OAuth2TokenValidatorResult.success();
                    }
                    return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_audience", "Missing required audience: fintech-gateway-api", null));
                }
            };
            jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
            return jwtDecoder;
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of("https://portal.fintech.bank", "https://mobile.fintech.bank"));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Correlation-Id"));
            config.setExposedHeaders(List.of("X-Correlation-Id"));
            config.setAllowCredentials(true);
            config.setMaxAge(Duration.ofHours(1));

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config);
            return source;
        }

        @Bean
        public AuthenticationEntryPoint customAuthenticationEntryPoint() {
            return (request, response, authException) -> {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                var error = new SecurityErrorResponse(
                    "https://fintech.bank/errors/unauthorized",
                    "Unauthorized Access",
                    HttpStatus.UNAUTHORIZED.value(),
                    authException.getMessage(),
                    Instant.now()
                );
                response.getWriter().write(objectMapper.writeValueAsString(error));
            };
        }

        @Bean
        public AccessDeniedHandler customAccessDeniedHandler() {
            return (request, response, accessDeniedException) -> {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
                var error = new SecurityErrorResponse(
                    "https://fintech.bank/errors/forbidden",
                    "Access Denied",
                    HttpStatus.FORBIDDEN.value(),
                    "Insufficient privileges to access this resource",
                    Instant.now()
                );
                response.getWriter().write(objectMapper.writeValueAsString(error));
            };
        }
    }

    // ============================================================================
    // 6. PROTECTED FINANCIAL GATEWAY CONTROLLER
    // ============================================================================
    @RestController
    @RequestMapping("/api/v1/transfers")
    public static class FinancialTransferController {

        @PostMapping
        @PreAuthorize("hasAuthority('SCOPE_transfers:execute') and hasRole('OPERATOR') and #request.amount <= 1000000.00")
        public TransferResponse executeTransfer(
                @RequestBody TransferRequest request,
                @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {
            
            String tokenTenant = jwt.getClaimAsString("tenant_id");
            if (tokenTenant == null || !tokenTenant.equals("FINTECH_CORP")) {
                throw new org.springframework.security.access.AccessDeniedException("Cross-tenant transaction violation");
            }

            return new TransferResponse(
                UUID.randomUUID().toString(),
                "SETTLED",
                Instant.now()
            );
        }
    }
}
