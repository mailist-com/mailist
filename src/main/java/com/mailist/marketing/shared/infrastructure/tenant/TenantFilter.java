package com.mailist.marketing.shared.infrastructure.tenant;

import com.mailist.marketing.shared.application.port.out.OrganizationRepository;
import com.mailist.marketing.shared.domain.aggregate.Organization;
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
                TenantContext.setSubdomain(tenantInfo.subdomain);
                
                log.debug("Tenant context set - Organization ID: {}, Subdomain: {}", 
                         tenantInfo.organizationId, tenantInfo.subdomain);
                
                // Continue with the request
                chain.doFilter(request, response);
            } else {
                // No valid tenant found
                log.warn("No valid tenant found for request: {}", httpRequest.getRequestURI());
                handleNoTenant(httpResponse, tenantInfo.subdomain);
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
                    return new TenantInfo(orgId, org.get().getSubdomain());
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid X-Organization-Id header: {}", orgIdHeader);
            }
        }
        
        // Strategy 2: Check subdomain header
        String subdomainHeader = request.getHeader("X-Subdomain");
        if (subdomainHeader != null) {
            Optional<Organization> org = organizationRepository.findBySubdomain(subdomainHeader);
            if (org.isPresent()) {
                return new TenantInfo(org.get().getId(), subdomainHeader);
            }
        }
        
        // Strategy 3: Extract from subdomain (e.g., company1.mailist.com)
        String subdomain = extractSubdomainFromHost(request);
        if (subdomain != null) {
            Optional<Organization> org = organizationRepository.findBySubdomain(subdomain);
            if (org.isPresent()) {
                return new TenantInfo(org.get().getId(), subdomain);
            }
            return new TenantInfo(null, subdomain); // Subdomain found but no org
        }
        
        // Strategy 4: Check if it's a test environment (localhost)
        if (isTestEnvironment(request)) {
            // For testing, use default organization or create one
            Optional<Organization> defaultOrg = organizationRepository.findBySubdomain("default");
            if (defaultOrg.isPresent()) {
                return new TenantInfo(defaultOrg.get().getId(), "default");
            }
        }
        
        return new TenantInfo(null, null);
    }
    
    /**
     * Extract subdomain from Host header
     */
    private String extractSubdomainFromHost(HttpServletRequest request) {
        String host = request.getHeader("Host");
        if (host == null) {
            return null;
        }
        
        // Remove port if present
        host = host.split(":")[0];
        
        // Check if it's a subdomain pattern (subdomain.domain.com)
        String[] parts = host.split("\\.");
        if (parts.length >= 3) {
            String subdomain = parts[0];
            
            // Skip common prefixes
            if (!subdomain.equals("www") && !subdomain.equals("api")) {
                return subdomain;
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
    private void handleNoTenant(HttpServletResponse response, String subdomain) throws IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.setContentType("application/json");
        
        String errorMessage = subdomain != null 
            ? String.format("{\"error\": \"Organization not found\", \"subdomain\": \"%s\"}", subdomain)
            : "{\"error\": \"No organization specified\"}";
            
        response.getWriter().write(errorMessage);
    }
    
    /**
     * Data class for tenant information
     */
    private static class TenantInfo {
        final Long organizationId;
        final String subdomain;
        
        TenantInfo(Long organizationId, String subdomain) {
            this.organizationId = organizationId;
            this.subdomain = subdomain;
        }
    }
}