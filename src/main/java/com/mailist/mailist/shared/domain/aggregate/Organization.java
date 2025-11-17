package com.mailist.mailist.shared.domain.aggregate;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Organization aggregate representing a tenant in the multi-tenant system.
 * Each organization has isolated data and can manage their own contacts, lists, campaigns, etc.
 */
@Entity
@Table(name = "organizations")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Organization {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Display name of the organization
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * Email of the organization owner/admin
     */
    @Column(nullable = false, unique = true)
    private String ownerEmail;
    
    /**
     * Subscription plan: FREE, PRO, ENTERPRISE
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Plan plan = Plan.FREE;
    
    /**
     * Maximum number of contacts allowed
     */
    @Column(name = "contact_limit")
    @Builder.Default
    private Integer contactLimit = 1000;
    
    /**
     * Maximum number of campaigns per month
     */
    @Column(name = "campaign_limit")
    @Builder.Default
    private Integer campaignLimit = 10;
    
    /**
     * Maximum number of automation rules
     */
    @Column(name = "automation_limit")
    @Builder.Default
    private Integer automationLimit = 5;
    
    /**
     * Organization status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.ACTIVE;
    
    /**
     * Additional settings as JSON
     */
    @Column(name = "settings", columnDefinition = "TEXT")
    private String settings;
    
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
     * Check if organization can add more contacts
     */
    public boolean canAddContact(int currentContactCount) {
        return currentContactCount < contactLimit;
    }
    
    /**
     * Check if organization can create more campaigns this month
     */
    public boolean canCreateCampaign(int currentMonthCampaigns) {
        return currentMonthCampaigns < campaignLimit;
    }
    
    /**
     * Check if organization can create more automation rules
     */
    public boolean canCreateAutomation(int currentAutomationCount) {
        return currentAutomationCount < automationLimit;
    }
    
    /**
     * Organization subscription plans
     */
    public enum Plan {
        FREE("Free Plan", 1000, 9000, 1, 10, 5),
        STANDARD("Standard Plan", -1, -1, 3, -1, -1),
        PRO("Pro Plan", -1, -1, -1, -1, -1); // -1 = unlimited

        private final String displayName;
        private final int contactLimit;
        private final int emailLimitPerMonth;
        private final int userLimit;
        private final int campaignLimit;
        private final int automationLimit;

        Plan(String displayName, int contactLimit, int emailLimitPerMonth, int userLimit,
             int campaignLimit, int automationLimit) {
            this.displayName = displayName;
            this.contactLimit = contactLimit;
            this.emailLimitPerMonth = emailLimitPerMonth;
            this.userLimit = userLimit;
            this.campaignLimit = campaignLimit;
            this.automationLimit = automationLimit;
        }

        public String getDisplayName() { return displayName; }
        public int getContactLimit() { return contactLimit; }
        public int getEmailLimitPerMonth() { return emailLimitPerMonth; }
        public int getUserLimit() { return userLimit; }
        public int getCampaignLimit() { return campaignLimit; }
        public int getAutomationLimit() { return automationLimit; }
    }
    
    /**
     * Organization status
     */
    public enum Status {
        ACTIVE,
        SUSPENDED,
        CANCELLED
    }
}