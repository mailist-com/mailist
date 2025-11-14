# Tenant Isolation Security in Automation System

## Overview
This document describes the multi-tenant security measures implemented to ensure that automations only execute on contacts within the same tenant (organization).

## Security Layers

### 1. Hibernate Tenant Filter (Database Level)
**File**: `TenantFilterAspect.java`

**What it does**:
- Automatically intercepts ALL Spring Data JPA repository calls
- Enables Hibernate `tenantFilter` for each query
- Adds `WHERE tenant_id = :tenantId` to all queries automatically

**Protection**: Prevents accidentally querying data from other tenants at the database level.

**Fixed Issue**: Corrected pointcut from `com.mailist.marketing.*` to `org.springframework.data.repository.Repository+.*` to work with Spring Data JPA.

### 2. Async Thread Context Propagation
**File**: `AsyncConfig.java`

**What it does**:
- Implements `TenantAwareTaskDecorator` that captures tenant context from parent thread
- Propagates `TenantContext` to @Async event handler threads
- Ensures ThreadLocal tenant context is available in async operations

**Protection**: Prevents loss of tenant context when automation events are processed asynchronously.

**Critical**: Without this, @Async methods would have NO tenant context and could access any tenant's data!

### 3. Explicit Tenant Validation (Application Level)
**File**: `AutomationExecutionService.java`

**What it does**:
```java
if (!automationRule.getTenantId().equals(contact.getTenantId())) {
    throw new SecurityException("Automation rule and contact must belong to the same tenant");
}
```

**Protection**: Defense-in-depth validation that explicitly checks tenant IDs match before executing automation.

**Benefit**: Even if Hibernate filter fails, this catches cross-tenant attempts.

## How It Works Together

### When Contact is Created:
1. User creates contact → `TenantContext` has current organization ID
2. `ContactCreatedEvent` published
3. `@Async` handler thread started → `TenantAwareTaskDecorator` copies tenant context
4. Handler finds automations → `TenantFilterAspect` enables filter → Only automations from SAME tenant returned
5. For each automation:
   - `startAutomation()` called
   - Hibernate filter ensures contact query filtered by tenant
   - **Explicit validation** checks `automationRule.tenantId == contact.tenantId`
   - Execution proceeds ONLY if same tenant

### Security Guarantees:
✅ Database queries automatically filtered by tenant_id (Layer 1)
✅ Async operations preserve tenant context (Layer 2)
✅ Explicit validation before execution (Layer 3)
✅ All entity persistence automatically sets tenant_id via `@PrePersist`

## Testing Tenant Isolation

To verify tenant isolation:
1. Create automation for Tenant A
2. Create contact for Tenant B
3. Manually call `automationExecutionService.startAutomation(ruleA, contactB, context)`
4. **Expected**: `SecurityException` thrown with log: "SECURITY VIOLATION"

## Potential Vulnerabilities (Now Fixed)

### ❌ Before Fixes:
- TenantFilterAspect pointcut didn't match repositories → **No tenant filtering!**
- @Async lost tenant context → **Could access any tenant!**
- No explicit validation → **Relied only on filter**

### ✅ After Fixes:
- All 3 security layers active
- Defense-in-depth approach
- Comprehensive logging for security auditing

## Logging

Security-relevant logs:
```
DEBUG: "Enabled tenant filter for tenant ID: {tenantId} in repository: {repository}"
WARN:  "No tenant context for repository call: {method}. This may be a security issue!"
ERROR: "SECURITY VIOLATION: Attempted to execute automation {id} (tenant {X}) on contact {id} (tenant {Y})"
```

## Maintenance

When adding new features that access cross-entity data:
1. ✅ Ensure entities extend `BaseTenantEntity`
2. ✅ Let Hibernate filter handle tenant filtering
3. ✅ Add explicit validation for critical operations
4. ✅ Use @Transactional for consistency
5. ✅ Test with multiple tenants

## Related Files
- `BaseTenantEntity.java` - Base class with tenant_id
- `TenantContext.java` - ThreadLocal tenant storage
- `TenantFilterAspect.java` - Hibernate filter enabler
- `AsyncConfig.java` - Async tenant context propagation
- `AutomationExecutionService.java` - Explicit validation
