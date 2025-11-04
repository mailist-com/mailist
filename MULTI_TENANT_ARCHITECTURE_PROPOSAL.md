# Multi-Tenant Architecture - Proposal for Mailist Platform

## Problem Analysis

Currently, the system has a critical flaw:
- `Contact.email` is globally unique across the entire system
- Users cannot subscribe to multiple lists from different account owners
- No separation between different customers using the platform

## Proposed Solution: Multi-Tenant Architecture

### 1. Add Organization/Account Entity

```java
@Entity
@Table(name = "organizations")
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String subdomain; // e.g., "company1", "startup2"
    
    @Column(nullable = false)
    private String name; // "Company Inc", "Startup Ltd"
    
    @Column(nullable = false, unique = true) 
    private String ownerEmail;
    
    // Plan limits, settings, etc.
    private String plan; // "FREE", "PRO", "ENTERPRISE"
    private Integer contactLimit;
    private Integer campaignLimit;
}
```

### 2. Update Contact Entity

```java
@Entity
@Table(name = "contacts", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "email"}))
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    
    // Email is now unique per organization, not globally
    @Column(nullable = false) // removed unique = true
    private String email;
    
    // ... rest of fields
}
```

### 3. Update ContactList Entity

```java
@Entity
@Table(name = "contact_lists")
public class ContactList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    
    @Column(nullable = false)
    private String name;
    
    // List is now scoped to organization
    @ManyToMany(mappedBy = "lists")
    private Set<Contact> contacts = new HashSet<>();
}
```

### 4. Update All Domain Entities

Add `organization` field to:
- ✅ Contact
- ✅ ContactList  
- ✅ Campaign
- ✅ AutomationRule
- ✅ Report

### 5. Add Tenant Context

```java
@Component
public class TenantContext {
    private static final ThreadLocal<Long> organizationId = new ThreadLocal<>();
    
    public static void setOrganizationId(Long orgId) {
        organizationId.set(orgId);
    }
    
    public static Long getOrganizationId() {
        return organizationId.get();
    }
    
    public static void clear() {
        organizationId.remove();
    }
}
```

### 6. Update Repository Interfaces

```java
public interface ContactRepository {
    Contact save(Contact contact);
    Optional<Contact> findByIdAndOrganizationId(Long id, Long organizationId);
    boolean existsByEmailAndOrganizationId(String email, Long organizationId);
    List<Contact> findByOrganizationId(Long organizationId);
    Page<Contact> findByOrganizationId(Long organizationId, Pageable pageable);
}
```

### 7. Add Tenant Filter

```java
@WebFilter("/*")
public class TenantFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // Extract organization from subdomain or header
        String subdomain = extractSubdomain(httpRequest);
        Long organizationId = organizationService.getIdBySubdomain(subdomain);
        
        try {
            TenantContext.setOrganizationId(organizationId);
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
```

## Database Schema Changes

### Migration Script

```sql
-- Add organizations table
CREATE TABLE organizations (
    id BIGSERIAL PRIMARY KEY,
    subdomain VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    owner_email VARCHAR(255) NOT NULL UNIQUE,
    plan VARCHAR(20) DEFAULT 'FREE',
    contact_limit INTEGER DEFAULT 1000,
    campaign_limit INTEGER DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add organization_id to existing tables
ALTER TABLE contacts ADD COLUMN organization_id BIGINT REFERENCES organizations(id);
ALTER TABLE contact_lists ADD COLUMN organization_id BIGINT REFERENCES organizations(id);
ALTER TABLE campaigns ADD COLUMN organization_id BIGINT REFERENCES organizations(id);
ALTER TABLE automation_rules ADD COLUMN organization_id BIGINT REFERENCES organizations(id);
ALTER TABLE reports ADD COLUMN organization_id BIGINT REFERENCES organizations(id);

-- Drop global unique constraint on email
ALTER TABLE contacts DROP CONSTRAINT contacts_email_key;

-- Add composite unique constraint (organization + email)
ALTER TABLE contacts ADD CONSTRAINT contacts_org_email_unique 
    UNIQUE (organization_id, email);

-- Create indexes for performance
CREATE INDEX idx_contacts_organization_id ON contacts(organization_id);
CREATE INDEX idx_contact_lists_organization_id ON contact_lists(organization_id);
CREATE INDEX idx_campaigns_organization_id ON campaigns(organization_id);
```

## Usage Examples

### 1. Multiple Organizations Scenario

```
Organization 1: "company1.mailist.com"
- Contact: john@example.com (in Company1's lists)

Organization 2: "startup2.mailist.com"  
- Contact: john@example.com (in Startup2's lists)

✅ Same email can exist in both organizations
✅ Completely isolated data
✅ Each organization manages their own contacts/lists
```

### 2. API Calls with Tenant Context

```http
# Company1's subdomain
POST https://company1.mailist.com/api/contacts
{
  "firstName": "John",
  "email": "john@example.com"
}

# Startup2's subdomain  
POST https://startup2.mailist.com/api/contacts
{
  "firstName": "John", 
  "email": "john@example.com"
}

✅ Both calls succeed
✅ Creates separate Contact records per organization
```

### 3. Contact List Subscriptions

```
User: john@example.com

Can subscribe to:
✅ Company1's "Newsletter" list
✅ Company1's "Promotions" list  
✅ Startup2's "Product Updates" list
✅ Startup2's "Weekly Tips" list

Each subscription creates separate Contact record per organization
```

## Implementation Priority

### Phase 1: Core Multi-tenancy
1. ✅ Add Organization entity
2. ✅ Update Contact with organization_id
3. ✅ Update ContactList with organization_id
4. ✅ Add TenantContext and Filter
5. ✅ Update repositories

### Phase 2: Extend to All Entities
1. ✅ Update Campaign with organization_id
2. ✅ Update AutomationRule with organization_id
3. ✅ Update Report with organization_id

### Phase 3: Advanced Features
1. ✅ Organization settings and limits
2. ✅ Billing integration
3. ✅ Advanced tenant isolation
4. ✅ Performance optimization

## Benefits

✅ **Scalability**: Support unlimited organizations
✅ **Data Isolation**: Complete separation between tenants
✅ **Flexibility**: Same email can subscribe to multiple organizations
✅ **Business Model**: SaaS-ready multi-tenant architecture
✅ **Security**: No data leakage between organizations
✅ **Performance**: Proper indexing and query optimization

## Backward Compatibility

For existing data migration:
1. Create default organization for existing data
2. Assign all existing contacts/lists to default organization
3. Gradually migrate to proper multi-tenant setup