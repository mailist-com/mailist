package com.mailist.mailist.shared.infrastructure.tenant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantFilterAspect {
    
    private final EntityManager entityManager;
    private final TenantIdentifierResolver tenantIdentifierResolver;
    
    @Around("execution(* com.mailist.marketing.*.infrastructure.repository.*RepositoryImpl.*(..))")
    public Object enableTenantFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        Long tenantId = tenantIdentifierResolver.getCurrentTenantId();
        
        if (tenantId != null) {
            Session session = entityManager.unwrap(Session.class);
            
            // Enable tenant filter for this session
            session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
            log.debug("Enabled tenant filter for tenant ID: {}", tenantId);
            
            try {
                return joinPoint.proceed();
            } finally {
                // Disable filter after operation
                session.disableFilter("tenantFilter");
                log.debug("Disabled tenant filter for tenant ID: {}", tenantId);
            }
        } else {
            // No tenant context, proceed without filter (for system operations)
            log.debug("No tenant context, proceeding without filter");
            return joinPoint.proceed();
        }
    }
}