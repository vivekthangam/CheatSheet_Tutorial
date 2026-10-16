package com.cheatsheet.boot.autoconfigure.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("AuditAutoConfiguration - ApplicationContextRunner Tests")
class AuditAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AuditAutoConfiguration.class));

    @Test
    @DisplayName("Should register InMemoryAuditService by default when matchIfMissing=true and no DataSource")
    void shouldRegisterInMemoryAuditServiceByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AuditService.class);
            assertThat(context).getBean(AuditService.class).isInstanceOf(InMemoryAuditService.class);

            AuditService auditService = context.getBean(AuditService.class);
            auditService.recordEvent("TEST_ACTION", "Testing default auto-config");
            List<AuditEvent> events = auditService.queryEvents("TEST_ACTION");
            assertThat(events).hasSize(1);
            assertThat(events.get(0).details()).isEqualTo("Testing default auto-config");
        });
    }

    @Test
    @DisplayName("Should back off when property cheatsheet.audit.enabled is false")
    void shouldBackOffWhenAuditPropertyDisabled() {
        contextRunner
                .withPropertyValues("cheatsheet.audit.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AuditService.class);
                    assertThat(context).doesNotHaveBean(AuditProperties.class);
                });
    }

    @Test
    @DisplayName("Should back off when user defines custom AuditService bean (@ConditionalOnMissingBean)")
    void shouldBackOffWhenUserDefinesCustomAuditService() {
        contextRunner
                .withUserConfiguration(CustomAuditConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(AuditService.class);
                    assertThat(context).getBean(AuditService.class).isInstanceOf(CustomAuditService.class);
                });
    }

    @Test
    @DisplayName("Should configure DatabaseAuditService when DataSource is present in context")
    void shouldConfigureDatabaseAuditServiceWhenDataSourcePresent() {
        contextRunner
                .withUserConfiguration(DataSourceMockConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(AuditService.class);
                    assertThat(context).getBean(AuditService.class).isInstanceOf(DatabaseAuditService.class);
                });
    }

    @Test
    @DisplayName("Should register Async Executor when async is enabled")
    void shouldRegisterAsyncExecutorWhenEnabled() {
        contextRunner
                .withPropertyValues("cheatsheet.audit.async=true")
                .run(context -> {
                    assertThat(context).hasBean("auditExecutor");
                });
    }

    @Test
    @DisplayName("Should fail or back off when required class is missing from ClassLoader")
    void shouldBackOffWhenClassFiltered() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(AuditService.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(AuditService.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomAuditConfiguration {
        @Bean
        AuditService customAuditService() {
            return new CustomAuditService();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class DataSourceMockConfiguration {
        @Bean
        DataSource dataSource() {
            return mock(DataSource.class);
        }
    }

    static class CustomAuditService implements AuditService {
        @Override
        public void recordEvent(String action, String details) {}

        @Override
        public List<AuditEvent> queryEvents(String action) {
            return List.of();
        }
    }
}
