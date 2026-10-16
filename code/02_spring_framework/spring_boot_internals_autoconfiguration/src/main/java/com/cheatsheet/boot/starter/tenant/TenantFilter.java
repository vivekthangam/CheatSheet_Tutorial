package com.cheatsheet.boot.starter.tenant;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Servlet filter intercepting inbound HTTP requests to establish tenant context.
 */
public class TenantFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);

    private final TenantProperties properties;

    public TenantFilter(TenantProperties properties) {
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        String tenantId = httpRequest.getHeader(properties.getHeaderName());

        if (tenantId == null || tenantId.isBlank()) {
            if (properties.isStrictMode()) {
                log.warn("Missing mandatory tenant header: {}", properties.getHeaderName());
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\":\"Missing required header: " + properties.getHeaderName() + "\"}");
                return;
            }
            tenantId = properties.getDefaultTenant();
        }

        TenantContextHolder.setTenantId(tenantId);
        httpResponse.setHeader(properties.getHeaderName(), tenantId);

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }
}
