package com.mailist.mailist.billing.domain.aggregate;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Usage tracking aggregate - tracks resource usage per tenant per month
 */
@Entity
@Table(name = "usage_tracking",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_usage_tracking_tenant_period",
           columnNames = {"tenant_id", "tracking_period"}
       ))
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsageTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tenant/Organization ID
     */
    @Column(nullable = false)
    private Long tenantId;

    /**
     * Tracking period (YYYY-MM format)
     */
    @Column(nullable = false, length = 7)
    private String trackingPeriod;

    /**
     * Current contact count
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer contactCount = 0;

    /**
     * Emails sent this month
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer emailSentCount = 0;

    /**
     * Campaigns created this month
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer campaignCount = 0;

    /**
     * Active automation rules
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer automationCount = 0;

    /**
     * Active user count
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer userCount = 0;

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
     * Increment contact count
     */
    public void incrementContactCount(int count) {
        this.contactCount += count;
    }

    /**
     * Decrement contact count
     */
    public void decrementContactCount(int count) {
        this.contactCount = Math.max(0, this.contactCount - count);
    }

    /**
     * Increment email sent count
     */
    public void incrementEmailSentCount(int count) {
        this.emailSentCount += count;
    }

    /**
     * Increment campaign count
     */
    public void incrementCampaignCount() {
        this.campaignCount++;
    }

    /**
     * Increment automation count
     */
    public void incrementAutomationCount() {
        this.automationCount++;
    }

    /**
     * Decrement automation count
     */
    public void decrementAutomationCount() {
        this.automationCount = Math.max(0, this.automationCount - 1);
    }

    /**
     * Set user count
     */
    public void setUserCount(int count) {
        this.userCount = count;
    }

    /**
     * Get current tracking period (YYYY-MM)
     */
    public static String getCurrentPeriod() {
        return YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    /**
     * Check if this is current period
     */
    public boolean isCurrentPeriod() {
        return trackingPeriod.equals(getCurrentPeriod());
    }

    /**
     * Reset monthly counters (for new month)
     */
    public void resetMonthlyCounters() {
        this.emailSentCount = 0;
        this.campaignCount = 0;
    }

    /**
     * Calculate usage percentage for resource
     */
    public int calculateUsagePercentage(String resource, int limit) {
        if (limit <= 0) {
            return 0; // Unlimited
        }

        int current = switch (resource.toLowerCase()) {
            case "contacts" -> contactCount;
            case "emails" -> emailSentCount;
            case "campaigns" -> campaignCount;
            case "automations" -> automationCount;
            case "users" -> userCount;
            default -> 0;
        };

        return (int) ((current * 100.0) / limit);
    }
}
