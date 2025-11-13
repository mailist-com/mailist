package com.mailist.mailist.shared.infrastructure.tenant;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@Slf4j
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver, HibernatePropertiesCustomizer {

    @Override
    public String resolveCurrentTenantIdentifier() {
        try {
            if (TenantContext.isSet()) {
                Long organizationId = TenantContext.getOrganizationId();
                log.debug("Resolved tenant ID: {}", organizationId);
                return String.valueOf(organizationId);
            }
        } catch (Exception e) {
            log.warn("Error resolving tenant identifier: {}", e.getMessage());
        }

        return "0";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}