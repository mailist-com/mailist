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
 * Supports multiple tenant identification strategies:
 * 1. Subdomain-based (e.g., company1.mailist.com)
 * 2. Header-based (X-Organization-Id or X-Subdomain)
 * 3. Path-based (for API testing)
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Execute before other filters
public class TenantFilter implements Filter {
    
    private final OrganizationRepository organizationRepository;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // Extract tenant information
            TenantInfo tenantInfo = extractTenantInfo(httpRequest);
            
            if (tenantInfo.organizationId != null) {
                // Set tenant context
                TenantContext.setOrganizationId(tenantInfo.organizationId);

                log.debug("Tenant context set - Organization ID: {}",
                         tenantInfo.organizationId);
                
                // Continue with the request
                chain.doFilter(request, response);
            } else {
                // No valid tenant found
                log.warn("No valid tenant found for request: {}", httpRequest.getRequestURI());
                handleNoTenant(httpResponse);
            }
            
        } finally {
            // Always clear context after request
            TenantContext.clear();
        }
    }
    
    /**
     * Extract tenant information from the request
     */
    private TenantInfo extractTenantInfo(HttpServletRequest request) {
        // Strategy 1: Check headers (for API testing and mobile apps)
        String orgIdHeader = request.getHeader("X-Organization-Id");
        if (orgIdHeader != null) {
            try {
                Long orgId = Long.parseLong(orgIdHeader);
                Optional<Organization> org = organizationRepository.findById(orgId);
                if (org.isPresent()) {
                    return new TenantInfo(orgId);
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid X-Organization-Id header: {}", orgIdHeader);
            }
        }
        
        return new TenantInfo(null);
    }
    
    /**
     * Handle request when no valid tenant is found
     */
    private void handleNoTenant(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("application/json");
        
        String errorMessage = "{\"error\": \"No organization specified\"}";
            
        response.getWriter().write(errorMessage);
    }
    
    /**
     * Data class for tenant information
     */
    private static class TenantInfo {
        final Long organizationId;

        TenantInfo(Long organizationId) {
            this.organizationId = organizationId;
        }
    }
}