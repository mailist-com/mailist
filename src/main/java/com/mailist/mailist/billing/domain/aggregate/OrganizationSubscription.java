package com.mailist.mailist.billing.domain.aggregate;

import com.mailist.mailist.billing.domain.valueobject.BillingCycle;
import com.mailist.mailist.billing.domain.valueobject.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Organization subscription aggregate - represents an active subscription
 */
@Entity
@Table(name = "organization_subscriptions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tenant/Organization ID
     */
    @Column(nullable = false)
    private Long tenantId;

    /**
     * Reference to subscription plan
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    private SubscriptionPlan subscriptionPlan;

    /**
     * Selected contact tier (for dynamic pricing)
     * Represents number of contacts in thousands (1 = 1000 contacts)
     */
    @Column(nullable = false)
    private Integer contactTier;

    /**
     * Calculated price in PLN
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePln;

    /**
     * Subscription status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    /**
     * Billing cycle
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    /**
     * Subscription start date
     */
    @Column(nullable = false)
    private LocalDateTime startDate;

    /**
     * Subscription end date (if cancelled or expired)
     */
    private LocalDateTime endDate;

    /**
     * Next billing date
     */
    private LocalDateTime nextBillingDate;

    /**
     * Cancellation timestamp
     */
    private LocalDateTime cancelledAt;

    /**
     * Reason for cancellation
     */
    @Column(columnDefinition = "TEXT")
    private String cancellationReason;

    /**
     * Payment provider (stripe, paypal, etc.)
     */
    @Column(nullable = false, length = 50)
    private String paymentProvider;

    /**
     * External subscription ID from payment provider
     */
    @Column(length = 255)
    private String externalSubscriptionId;

    /**
     * External customer ID from payment provider
     */
    @Column(length = 255)
    private String externalCustomerId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Set default dates if not provided
        if (startDate == null) {
            startDate = LocalDateTime.now();
        }
        if (nextBillingDate == null) {
            nextBillingDate = calculateNextBillingDate();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if subscription is active
     */
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE
                && (endDate == null || endDate.isAfter(LocalDateTime.now()));
    }

    /**
     * Check if subscription is cancelled but still valid
     */
    public boolean isCancelledButValid() {
        return status == SubscriptionStatus.CANCELLED
                && endDate != null
                && endDate.isAfter(LocalDateTime.now());
    }

    /**
     * Cancel subscription
     */
    public void cancel(String reason) {
        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
        // Set end date to next billing date
        this.endDate = this.nextBillingDate;
    }

    /**
     * Suspend subscription
     */
    public void suspend() {
        this.status = SubscriptionStatus.SUSPENDED;
    }

    /**
     * Reactivate subscription
     */
    public void reactivate() {
        this.status = SubscriptionStatus.ACTIVE;
        this.cancelledAt = null;
        this.cancellationReason = null;
        this.endDate = null;
    }

    /**
     * Mark as expired
     */
    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
        this.endDate = LocalDateTime.now();
    }

    /**
     * Mark as past due
     */
    public void markPastDue() {
        this.status = SubscriptionStatus.PAST_DUE;
    }

    /**
     * Upgrade/downgrade plan
     */
    public void changePlan(SubscriptionPlan newPlan, int newContactTier) {
        this.subscriptionPlan = newPlan;
        this.contactTier = newContactTier;
        this.pricePln = newPlan.calculatePrice(newContactTier);
    }

    /**
     * Calculate next billing date based on billing cycle
     */
    public LocalDateTime calculateNextBillingDate() {
        LocalDateTime baseDate = startDate != null ? startDate : LocalDateTime.now();
        return baseDate.plusMonths(billingCycle.getMonths());
    }

    /**
     * Update next billing date after successful payment
     */
    public void updateNextBillingDate() {
        this.nextBillingDate = nextBillingDate.plusMonths(billingCycle.getMonths());
    }

    /**
     * Get effective contact limit
     */
    public int getEffectiveContactLimit() {
        if (subscriptionPlan.isUnlimitedContacts()) {
            return Integer.MAX_VALUE;
        }
        // For paid plans with dynamic pricing, use tier * 1000
        if (contactTier > 0) {
            return contactTier * 1000;
        }
        return subscriptionPlan.getContactLimit();
    }

    /**
     * Get effective email limit
     */
    public int getEffectiveEmailLimit() {
        if (subscriptionPlan.isUnlimitedEmails()) {
            return Integer.MAX_VALUE;
        }
        return subscriptionPlan.getEmailLimitPerMonth();
    }

    /**
     * Get effective user limit
     */
    public int getEffectiveUserLimit() {
        if (subscriptionPlan.isUnlimitedUsers()) {
            return Integer.MAX_VALUE;
        }
        return subscriptionPlan.getUserLimit();
    }

    /**
     * Check if can add more contacts
     */
    public boolean canAddContacts(int currentCount, int toAdd) {
        int effectiveLimit = getEffectiveContactLimit();
        return (currentCount + toAdd) <= effectiveLimit;
    }

    /**
     * Check if can send more emails
     */
    public boolean canSendEmails(int currentCount, int toSend) {
        int effectiveLimit = getEffectiveEmailLimit();
        return (currentCount + toSend) <= effectiveLimit;
    }

    /**
     * Check if can add more users
     */
    public boolean canAddUsers(int currentCount, int toAdd) {
        int effectiveLimit = getEffectiveUserLimit();
        return (currentCount + toAdd) <= effectiveLimit;
    }
}
