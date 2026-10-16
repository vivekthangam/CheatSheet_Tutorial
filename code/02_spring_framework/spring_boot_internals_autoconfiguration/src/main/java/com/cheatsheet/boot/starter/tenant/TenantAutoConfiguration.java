package com.cheatsheet.boot.starter.tenant;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Auto-configuration for Multi-Tenancy Starter.
 * Automatically activates when:
 * 1. multitenancy.enabled is true (or missing/defaulted)
 * 2. Servlet Filter and DataSource classes are on the classpath
 * Runs BEFORE Spring Boot's DataSourceAutoConfiguration.
 */
@AutoConfiguration(before = DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(TenantProperties.class)
@ConditionalOnProperty(prefix = "multitenancy", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({Filter.class, DataSource.class})
public class TenantAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TenantAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(TenantFilter.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public FilterRegistrationBean<TenantFilter> tenantFilterRegistration(TenantProperties properties) {
        log.info("Auto-configuring TenantFilter with header: [{}] and defaultTenant: [{}]",
                properties.getHeaderName(), properties.getDefaultTenant());

        TenantFilter filter = new TenantFilter(properties);
        FilterRegistrationBean<TenantFilter> registrationBean = new FilterRegistrationBean<>(filter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "dataSource")
    @ConditionalOnProperty(prefix = "multitenancy", name = "datasource-routing", havingValue = "true", matchIfMissing = false)
    public DataSource multiTenantRoutingDataSource(TenantProperties properties) {
        log.info("Auto-configuring MultiTenantRoutingDataSource for {} tenant definitions",
                properties.getTenants().size());

        Map<Object, Object> targetDataSources = new HashMap<>();
        DataSource defaultDataSource = null;

        for (Map.Entry<String, TenantProperties.TenantDataSourceConfig> entry : properties.getTenants().entrySet()) {
            String tenantKey = entry.getKey();
            TenantProperties.TenantDataSourceConfig cfg = entry.getValue();

            HikariDataSource ds = new HikariDataSource();
            ds.setPoolName("HikariPool-" + tenantKey);
            ds.setJdbcUrl(cfg.getUrl());
            ds.setUsername(cfg.getUsername());
            ds.setPassword(cfg.getPassword());
            ds.setDriverClassName(cfg.getDriverClassName());

            targetDataSources.put(tenantKey, ds);

            if (tenantKey.equalsIgnoreCase(properties.getDefaultTenant())) {
                defaultDataSource = ds;
            }
        }

        if (defaultDataSource == null && !targetDataSources.isEmpty()) {
            defaultDataSource = (DataSource) targetDataSources.values().iterator().next();
        }

        if (defaultDataSource == null) {
            // Fallback in-memory datasource if none defined
            HikariDataSource fallback = new HikariDataSource();
            fallback.setPoolName("HikariPool-default-fallback");
            fallback.setJdbcUrl("jdbc:h2:mem:defaultdb;DB_CLOSE_DELAY=-1");
            fallback.setUsername("sa");
            fallback.setPassword("");
            fallback.setDriverClassName("org.h2.Driver");
            defaultDataSource = fallback;
            targetDataSources.put(properties.getDefaultTenant(), fallback);
        }

        return new MultiTenantRoutingDataSource(defaultDataSource, targetDataSources, properties.getDefaultTenant());
    }
}
