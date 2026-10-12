package com.cheatsheet.security.securitylab;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/**
 * Real-World Security Vulnerabilities & Mitigations (SecurityLab)
 */
public class SecuritySecurityLab {

    // =========================================================================
    // VULNERABILITY 1: Insecure JWT Algorithm Whitelisting (Algorithm Confusion)
    // =========================================================================

    @Configuration
    public static class AlgorithmConfusionDefense {
        // 🚨 VULNERABILITY: Permitting arbitrary JWS algorithms without whitelist
        public JwtDecoder vulnerableDecoder() {
            // Allows attacker to submit 'alg: none' or HMAC with public RSA key
            return NimbusJwtDecoder.withJwkSetUri("https://auth.fintech.bank/.well-known/jwks.json").build();
        }

        // 🛡️ MITIGATION: Enforce strict cryptographic algorithm constraints (RS256 / ES256 only)
        @Bean
        public JwtDecoder secureJwtDecoder() {
            NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri("https://auth.fintech.bank/.well-known/jwks.json")
                .jwsAlgorithms(algorithms -> {
                    algorithms.clear();
                    algorithms.add(SignatureAlgorithm.RS256);
                    algorithms.add(SignatureAlgorithm.ES256);
                })
                .build();

            jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("https://auth.fintech.bank"));
            return jwtDecoder;
        }
    }

    // =========================================================================
    // VULNERABILITY 2: Insecure Direct Object Reference (IDOR) / BOLA
    // =========================================================================

    public interface AccountRepository {
        boolean isOwner(String accountId, String userId);
    }

    @Component("accountSecurityGuard")
    public static class AccountSecurityGuard {
        private final AccountRepository accountRepository;

        public AccountSecurityGuard(AccountRepository accountRepository) {
            this.accountRepository = accountRepository;
        }

        public boolean isAccountOwner(String accountId, Authentication authentication) {
            return accountRepository.isOwner(accountId, authentication.getName());
        }
    }

    @RestController
    public static class AccountWithdrawController {
        // 🚨 VULNERABILITY: Role check without resource-level ownership validation
        @PostMapping("/accounts/insecure/{accountId}/withdraw")
        @PreAuthorize("hasRole('USER')") // 💥 Any user can withdraw from another user's account!
        public ResponseEntity<Void> withdrawVulnerable(@PathVariable String accountId, @RequestParam BigDecimal amount) {
            return ResponseEntity.ok().build();
        }

        // 🛡️ MITIGATION: Enforce Object-Level Authorization using SpEL and SecurityContext principal
        @PostMapping("/accounts/secure/{accountId}/withdraw")
        @PreAuthorize("hasRole('USER') and @accountSecurityGuard.isAccountOwner(#accountId, authentication)")
        public ResponseEntity<Void> withdrawHardened(@PathVariable String accountId, @RequestParam BigDecimal amount) {
            return ResponseEntity.ok().build();
        }
    }

    // =========================================================================
    // VULNERABILITY 3: Wildcard CORS with Credentials Sharing
    // =========================================================================

    @Configuration
    public static class CorsHardeningConfig {
        // 🚨 VULNERABILITY: Wildcard origin with credential sharing
        public CorsConfigurationSource dangerousCorsSource() {
            CorsConfiguration config = new CorsConfiguration();
            config.addAllowedOrigin("*"); // 💥 Allows evil domains to trigger authenticated actions
            config.setAllowCredentials(true);
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config);
            return source;
        }

        // 🛡️ MITIGATION: Explicit origin whitelist with strict method and header restrictions
        @Bean
        public CorsConfigurationSource hardenedCorsSource() {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of(
                "https://banking.fintech.bank",
                "https://admin.fintech.bank"
            ));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
            config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
            config.setAllowCredentials(true);
            config.setMaxAge(Duration.ofMinutes(30));

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/api/**", config);
            return source;
        }
    }
}
