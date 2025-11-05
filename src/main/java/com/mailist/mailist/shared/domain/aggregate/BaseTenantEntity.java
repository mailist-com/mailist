package com.mailist.mailist.shared.domain.aggregate;

import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import jakarta.persistence.*;

@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
public abstract class BaseTenantEntity {
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @PrePersist
    @PreUpdate
    protected void setTenantId() {
        if (tenantId == null && TenantContext.isSet()) {
            this.tenantId = TenantContext.getOrganizationId();
        }
    }
}