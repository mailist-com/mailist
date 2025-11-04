package com.mailist.marketing.shared.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
    "com.mailist.marketing.**.infrastructure.repository", 
    "com.mailist.marketing.shared.infrastructure.repository"
})
@EntityScan(basePackages = {
    "com.mailist.marketing.**.domain.aggregate",
    "com.mailist.marketing.shared.domain.aggregate"
})
@EnableAspectJAutoProxy
public class JpaConfig {
    
    // Row-level multi-tenancy using Hibernate filters
    // Configuration is handled by @Filter annotations on entities
    // and TenantFilterAspect for automatic filter enablement
    
}