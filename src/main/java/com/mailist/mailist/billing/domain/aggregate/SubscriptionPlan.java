package com.mailist.mailist.billing.domain.aggregate;

import com.mailist.mailist.billing.domain.valueobject.SubscriptionPlanType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Subscription plan aggregate - represents a subscription tier
 */
@Entity
@Table(name = "subscription_plans")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique plan name (FREE, STANDARD, PRO)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Display name for UI
     */
    @Column(nullable = false, length = 100)
    private String displayName;

    /**
     * Plan description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Contact limit (-1 for unlimited)
     */
    @Column(nullable = false)
    private Integer contactLimit;

    /**
     * Email limit per month (-1 for unlimited)
     */
    @Column(nullable = false)
    private Integer emailLimitPerMonth;

    /**
     * User limit (-1 for unlimited)
     */
    @Column(nullable = false)
    private Integer userLimit;

    /**
     * Campaign limit (-1 for unlimited)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer campaignLimit = -1;

    /**
     * Automation limit (-1 for unlimited)
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer automationLimit = -1;

    /**
     * Base price in PLN
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePricePln;

    /**
     * Price per 1000 contacts in PLN
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePer1000ContactsPln;

    /**
     * Is plan currently active/available
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;
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

    /**
     * Check if contact limit is unlimited
     */
    public boolean isUnlimitedContacts() {
        return contactLimit == -1;
    }

    /**
     * Check if email limit is unlimited
     */
    public boolean isUnlimitedEmails() {
        return emailLimitPerMonth == -1;
    }

    /**
     * Check if user limit is unlimited
     */
    public boolean isUnlimitedUsers() {
        return userLimit == -1;
    }

    /**
     * Calculate price for given contact tier
     *
     * @param contactTier Number of contacts in thousands (1 = 1000 contacts)
     * @return Total price in PLN
     */
    public BigDecimal calculatePrice(int contactTier) {
        if (name.equals("FREE")) {
            return BigDecimal.ZERO;
        }

        BigDecimal tierPrice = pricePer1000ContactsPln.multiply(BigDecimal.valueOf(contactTier - 1));
        return basePricePln.add(tierPrice);
    }

    /**
     * Get plan type enum
     */
    public SubscriptionPlanType getPlanType() {
        return SubscriptionPlanType.valueOf(name);
    }

    /**
     * Check if usage is within limits
     */
    public boolean isWithinContactLimit(int currentContacts) {
        return isUnlimitedContacts() || currentContacts <= contactLimit;
    }

    public boolean isWithinEmailLimit(int currentEmails) {
        return isUnlimitedEmails() || currentEmails <= emailLimitPerMonth;
    }

    public boolean isWithinUserLimit(int currentUsers) {
        return isUnlimitedUsers() || currentUsers <= userLimit;
    }

    /**
     * Calculate usage percentage
     */
    public int getContactUsagePercentage(int currentContacts) {
        if (isUnlimitedContacts()) {
            return 0;
        }
        return (int) ((currentContacts * 100.0) / contactLimit);
    }

    public int getEmailUsagePercentage(int currentEmails) {
        if (isUnlimitedEmails()) {
            return 0;
        }
        return (int) ((currentEmails * 100.0) / emailLimitPerMonth);
    }
}
