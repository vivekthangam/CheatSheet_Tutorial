package com.cheatsheet.boot.autoconfigure.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RateLimiterAutoConfiguration - ApplicationContextRunner Tests")
class RateLimiterAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RateLimiterAutoConfiguration.class));

    @Test
    @DisplayName("Should auto-configure TokenBucketRateLimiter by default")
    void shouldAutoConfigureByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RateLimiter.class);
            assertThat(context).getBean(RateLimiter.class).isInstanceOf(TokenBucketRateLimiter.class);

            RateLimiter limiter = context.getBean(RateLimiter.class);
            assertThat(limiter.tryAcquire("user-1")).isTrue();
        });
    }

    @Test
    @DisplayName("Should back off when cheatsheet.ratelimit.enabled=false")
    void shouldBackOffWhenDisabled() {
        contextRunner
                .withPropertyValues("cheatsheet.ratelimit.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RateLimiter.class);
                });
    }

    @Test
    @DisplayName("Should allow custom RateLimiter to override default")
    void shouldAllowCustomRateLimiter() {
        contextRunner
                .withUserConfiguration(CustomRateLimiterConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(RateLimiter.class);
                    assertThat(context).getBean(RateLimiter.class).isInstanceOf(CustomRateLimiter.class);
                });
    }

    @Test
    @DisplayName("Should bind customized properties properly")
    void shouldBindCustomizedProperties() {
        contextRunner
                .withPropertyValues(
                        "cheatsheet.ratelimit.requests-per-second=200",
                        "cheatsheet.ratelimit.burst-capacity=500"
                )
                .run(context -> {
                    RateLimiterProperties props = context.getBean(RateLimiterProperties.class);
                    assertThat(props.getRequestsPerSecond()).isEqualTo(200);
                    assertThat(props.getBurstCapacity()).isEqualTo(500);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomRateLimiterConfiguration {
        @Bean
        RateLimiter customRateLimiter() {
            return new CustomRateLimiter();
        }
    }

    static class CustomRateLimiter implements RateLimiter {
        @Override
        public boolean tryAcquire(String clientId) {
            return true;
        }

        @Override
        public int getAvailableTokens(String clientId) {
            return Integer.MAX_VALUE;
        }

        @Override
        public void reset(String clientId) {}
    }
}
