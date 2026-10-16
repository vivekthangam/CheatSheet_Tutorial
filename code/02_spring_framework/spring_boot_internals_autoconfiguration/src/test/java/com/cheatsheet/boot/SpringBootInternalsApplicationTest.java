package com.cheatsheet.boot;

import com.cheatsheet.boot.autoconfigure.audit.AuditService;
import com.cheatsheet.boot.autoconfigure.ratelimit.RateLimiter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("SpringBootInternalsApplication - Context Loads & Bean Wiring")
class SpringBootInternalsApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private RateLimiter rateLimiter;

    @Autowired(required = false)
    private AuditService auditService;

    @Test
    @DisplayName("Context loads successfully and auto-configured beans are present")
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
        assertThat(rateLimiter).isNotNull();
        assertThat(auditService).isNotNull();

        // Verify bean operation
        assertThat(rateLimiter.tryAcquire("test-client")).isTrue();
        auditService.record("TEST_ACTION", "integration-test");
    }
}
