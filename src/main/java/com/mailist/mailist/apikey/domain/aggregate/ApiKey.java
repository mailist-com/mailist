package com.mailist.mailist.apikey.domain.aggregate;

import com.mailist.mailist.apikey.domain.valueobject.ApiKeyStatus;
import com.mailist.mailist.shared.domain.aggregate.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * API Key aggregate.
 * Represents an API key that allows external applications to integrate with Mailist.
 */
@Entity
@Table(name = "api_keys", indexes = {
        @Index(name = "idx_api_key_hash", columnList = "keyHash"),
        @Index(name = "idx_api_key_prefix", columnList = "keyPrefix"),
        @Index(name = "idx_api_key_org", columnList = "organizationId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    /**
     * Hashed API key (SHA-256).
     * Never store the plain text key!
     */
    @Column(nullable = false, unique = true, length = 64)
    private String keyHash;

    /**
     * Key prefix for identification (e.g., "ml_live_", "ml_test_").
     */
    @Column(nullable = false, length = 20)
    private String keyPrefix;

    /**
     * Last 4 characters of the key for display purposes.
     */
    @Column(nullable = false, length = 4)
    private String lastFourChars;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ApiKeyStatus status = ApiKeyStatus.ACTIVE;

    /**
     * Permissions granted to this API key.
     * Examples: "contacts.read", "contacts.write", "campaigns.read", "campaigns.write"
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "api_key_permissions", joinColumns = @JoinColumn(name = "api_key_id"))
    @Column(name = "permission", length = 100)
    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    /**
     * User ID who created this API key.
     */
    @Column(nullable = false)
    private String createdBy;

    /**
     * Last time this API key was used.
     */
    @Column
    private LocalDateTime lastUsedAt;

    /**
     * IP address from last use.
     */
    @Column(length = 45)
    private String lastUsedIpAddress;

    /**
     * Total number of API calls made with this key.
     */
    @Column(nullable = false)
    @Builder.Default
    private Long totalCalls = 0L;

    /**
     * Expiration date (optional).
     */
    @Column
    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic methods

    public void revoke() {
        this.status = ApiKeyStatus.REVOKED;
    }

    public void recordUsage(String ipAddress) {
        this.lastUsedAt = LocalDateTime.now();
        this.lastUsedIpAddress = ipAddress;
        this.totalCalls++;
    }

    public boolean isActive() {
        if (this.status != ApiKeyStatus.ACTIVE) {
            return false;
        }
        if (this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt)) {
            this.status = ApiKeyStatus.EXPIRED;
            return false;
        }
        return true;
    }

    public boolean hasPermission(String permission) {
        // Support wildcard permissions like "contacts.*"
        if (permissions.contains(permission)) {
            return true;
        }

        // Check for wildcard
        String[] parts = permission.split("\\.");
        if (parts.length == 2) {
            String wildcardPermission = parts[0] + ".*";
            if (permissions.contains(wildcardPermission)) {
                return true;
            }
        }

        // Check for admin permission
        return permissions.contains("*") || permissions.contains("admin.*");
    }

    public void addPermission(String permission) {
        this.permissions.add(permission);
    }

    public void removePermission(String permission) {
        this.permissions.remove(permission);
    }

    public String getDisplayKey() {
        return keyPrefix + "..." + lastFourChars;
    }
}
