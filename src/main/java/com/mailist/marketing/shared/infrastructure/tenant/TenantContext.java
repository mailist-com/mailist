package com.mailist.marketing.shared.infrastructure.tenant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Thread-local storage for the current tenant (organization) context.
 * This ensures that all operations within a request are scoped to the correct organization.
 */
@Component
@Slf4j
public class TenantContext {
    
    private static final ThreadLocal<Long> currentOrganizationId = new ThreadLocal<>();
    private static final ThreadLocal<String> currentSubdomain = new ThreadLocal<>();
    
    /**
     * Set the current organization ID for this thread
     */
    public static void setOrganizationId(Long organizationId) {
        log.debug("Setting organization ID to: {}", organizationId);
        currentOrganizationId.set(organizationId);
    }
    
    /**
     * Get the current organization ID for this thread
     */
    public static Long getOrganizationId() {
        Long orgId = currentOrganizationId.get();
        if (orgId == null) {
            throw new IllegalStateException("No organization context set. Make sure TenantFilter is properly configured.");
        }
        return orgId;
    }
    
    /**
     * Set the current subdomain for this thread
     */
    public static void setSubdomain(String subdomain) {
        log.debug("Setting subdomain to: {}", subdomain);
        currentSubdomain.set(subdomain);
    }
    
    /**
     * Get the current subdomain for this thread
     */
    public static String getSubdomain() {
        return currentSubdomain.get();
    }
    
    /**
     * Check if organization context is set
     */
    public static boolean isSet() {
        return currentOrganizationId.get() != null;
    }
    
    /**
     * Clear the tenant context for this thread
     * Should be called at the end of each request
     */
    public static void clear() {
        log.debug("Clearing tenant context");
        currentOrganizationId.remove();
        currentSubdomain.remove();
    }
    
    /**
     * Execute a block of code with a specific organization context
     */
    public static <T> T executeWithOrganization(Long organizationId, TenantOperation<T> operation) {
        Long previousOrgId = currentOrganizationId.get();
        try {
            setOrganizationId(organizationId);
            return operation.execute();
        } finally {
            if (previousOrgId != null) {
                setOrganizationId(previousOrgId);
            } else {
                clear();
            }
        }
    }
    
    @FunctionalInterface
    public interface TenantOperation<T> {
        T execute();
    }
}