package com.cheatsheet.boot.starter.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Dynamic Multi-Tenant Routing DataSource.
 * Intercepts connection acquisition and routes to tenant-specific DataSource.
 */
public class MultiTenantRoutingDataSource extends AbstractRoutingDataSource {

    private static final Logger log = LoggerFactory.getLogger(MultiTenantRoutingDataSource.class);

    private final String defaultTenant;

    public MultiTenantRoutingDataSource(DataSource defaultDataSource,
                                        Map<Object, Object> targetDataSources,
                                        String defaultTenant) {
        this.defaultTenant = defaultTenant;
        setDefaultTargetDataSource(defaultDataSource);
        setTargetDataSources(targetDataSources);
        afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = defaultTenant;
        }
        log.debug("Routing database connection to tenant: {}", tenantId);
        return tenantId;
    }
}
