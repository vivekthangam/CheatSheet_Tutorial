package com.cheatsheet.ioc.securitylab;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.math.BigDecimal;

/**
 * 🛡️ IoC SecurityLab: Security Vulnerabilities & Hardened Mitigations
 */
public class IocSecurityLab {

    public record OrderDiscountContext(BigDecimal baseAmount, int customerTier) {}

    // =========================================================================
    // SEC 1: SpEL Injection via Dynamic Expression Evaluation (RCE)
    // =========================================================================
    public static class Sec1_SpelInjection {

        // 🚨 VULNERABILITY: StandardEvaluationContext grants full JVM reflection access to SpEL
        public static BigDecimal vulnerableCalculateDiscount(String userFormula, OrderDiscountContext ctx) {
            ExpressionParser parser = new SpelExpressionParser();
            StandardEvaluationContext context = new StandardEvaluationContext(ctx);
            // 💥 RCE: Attacker executes arbitrary Java classes via T(...) expressions
            return (BigDecimal) parser.parseExpression(userFormula).getValue(context);
        }

        // 🛡️ MITIGATION: Restrict SpEL to read-only SimpleEvaluationContext with strict regex guard
        public static BigDecimal secureCalculateDiscount(String userFormula, OrderDiscountContext ctx) {
            // 1. Strict regex whitelist allowing only arithmetic and variable references
            if (!userFormula.matches("^[a-zA-Z0-9_+\\-*/().\\s]+$")) {
                throw new SecurityException("Illegal characters detected in discount formula");
            }

            ExpressionParser parser = new SpelExpressionParser();
            // 2. SimpleEvaluationContext blocks Class loading, Constructors and arbitrary Method invocation
            EvaluationContext safeContext = SimpleEvaluationContext.forReadOnlyDataBinding()
                    .withRootObject(ctx)
                    .build();

            return (BigDecimal) parser.parseExpression(userFormula).getValue(safeContext);
        }
    }

    // =========================================================================
    // SEC 2: Actuator Endpoint Hardening
    // =========================================================================
    @Configuration
    public static class Sec2_ActuatorSecurityConfig {

        // 🛡️ MITIGATION: Restrict administrative endpoints behind authentication
        @Bean
        public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .securityMatcher(EndpointRequest.toAnyEndpoint())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll() // Only /health is public
                            .anyRequest().hasRole("OPS_ADMIN") // All others require OPS_ADMIN role
                    )
                    .httpBasic(Customizer.withDefaults())
                    .build();
        }
    }
}
