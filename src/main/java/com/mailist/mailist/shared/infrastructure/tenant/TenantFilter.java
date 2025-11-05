package com.mailist.mailist.shared.infrastructure.tenant;

import com.mailist.mailist.shared.application.port.out.OrganizationRepository;
import com.mailist.mailist.shared.domain.aggregate.Organization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Filter that extracts tenant information from the request and sets up the tenant context.
 * Supports tenant identification via:
 * 1. Header-based (X-Organization-Id) - primary method
 * 2. Default organization for localhost/test environments
 *
 * Skips public endpoints that don't require tenant context (auth, tracking, etc.)
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Execute before other filters
public class TenantFilter implements Filter {

    private final OrganizationRepository organizationRepository;

    // Paths that don't require tenant context
    private static final String[] EXCLUDED_PATHS = {
        "/api/v1/auth/",           // All auth endpoints (register, login, etc.)
        "/api/tracking/",          // Email tracking endpoints (open, click)
        "/swagger-ui/",            // Swagger UI
        "/v3/api-docs",            // OpenAPI docs
        "/actuator/",              // Spring Boot Actuator
        "/error",                  // Error handling
        "/favicon.ico"             // Browser favicon requests
    };
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestPath = httpRequest.getRequestURI();

        // Skip tenant resolution for public endpoints
        if (isExcludedPath(requestPath)) {
            log.debug("Skipping tenant filter for public endpoint: {}", requestPath);
            chain.doFilter(request, response);
            return;
        }

        try {
            // Extract tenant information
            Long organizationId = extractOrganizationId(httpRequest);

            if (organizationId != null) {
                // Set tenant context
                TenantContext.setOrganizationId(organizationId);

                log.debug("Tenant context set - Organization ID: {}", organizationId);

                // Continue with the request
                chain.doFilter(request, response);
            } else {
                // No valid tenant found
                log.warn("No valid tenant found for request: {}", requestPath);
                handleNoTenant(httpResponse);
            }

        } finally {
            // Always clear context after request
            TenantContext.clear();
        }
    }

    /**
     * Check if the request path should skip tenant resolution
     */
    private boolean isExcludedPath(String requestPath) {
        for (String excludedPath : EXCLUDED_PATHS) {
            if (requestPath.startsWith(excludedPath)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Extract organization ID from the request
     */
    private Long extractOrganizationId(HttpServletRequest request) {
        // Strategy 1: Check X-Organization-Id header (primary method)
        String orgIdHeader = request.getHeader("X-Organization-Id");
        if (orgIdHeader != null) {
            try {
                Long orgId = Long.parseLong(orgIdHeader);
                Optional<Organization> org = organizationRepository.findById(orgId);
                if (org.isPresent()) {
                    return orgId;
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid X-Organization-Id header: {}", orgIdHeader);
            }
        }

        // Strategy 2: For test/development environment (localhost), use default organization
        if (isTestEnvironment(request)) {
            Optional<Organization> defaultOrg = organizationRepository.findById(1L);
            if (defaultOrg.isPresent()) {
                log.debug("Using default organization ID: 1 for test environment");
                return 1L;
            }
        }

        return null;
    }

    /**
     * Check if request is from test environment
     */
    private boolean isTestEnvironment(HttpServletRequest request) {
        String host = request.getHeader("Host");
        return host != null && (host.contains("localhost") || host.contains("127.0.0.1"));
    }

    /**
     * Handle request when no valid tenant is found
     */
    private void handleNoTenant(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");

        String errorMessage = "{\"error\": \"No organization specified. Please provide X-Organization-Id header.\"}";

        response.getWriter().write(errorMessage);
    }
}