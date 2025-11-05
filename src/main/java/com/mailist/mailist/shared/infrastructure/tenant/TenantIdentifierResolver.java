package com.mailist.mailist.shared.infrastructure.tenant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TenantIdentifierResolver {
    
    public Long getCurrentTenantId() {
        try {
            if (TenantContext.isSet()) {
                Long organizationId = TenantContext.getOrganizationId();
                log.debug("Resolved tenant ID: {}", organizationId);
                return organizationId;
            }
        } catch (Exception e) {
            log.warn("Error resolving tenant identifier: {}", e.getMessage());
        }
        
        return null; // No tenant context
    }
}