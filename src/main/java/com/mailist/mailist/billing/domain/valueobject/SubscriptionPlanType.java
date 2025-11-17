package com.mailist.mailist.billing.domain.valueobject;

/**
 * Subscription plan types with their limits and pricing
 */
public enum SubscriptionPlanType {
    FREE("Free Plan", 1000, 9000, 1, 10, 5, 0.0, 0.0),
    STANDARD("Standard Plan", -1, -1, 3, -1, -1, 49.0, 49.0),
    PRO("Pro Plan", -1, -1, -1, -1, -1, 99.0, 99.0);

    private final String displayName;
    private final int contactLimit;
    private final int emailLimitPerMonth;
    private final int userLimit;
    private final int campaignLimit;
    private final int automationLimit;
    private final double basePricePln;
    private final double pricePer1000ContactsPln;

    SubscriptionPlanType(String displayName, int contactLimit, int emailLimitPerMonth,
                         int userLimit, int campaignLimit, int automationLimit,
                         double basePricePln, double pricePer1000ContactsPln) {
        this.displayName = displayName;
        this.contactLimit = contactLimit;
        this.emailLimitPerMonth = emailLimitPerMonth;
        this.userLimit = userLimit;
        this.campaignLimit = campaignLimit;
        this.automationLimit = automationLimit;
        this.basePricePln = basePricePln;
        this.pricePer1000ContactsPln = pricePer1000ContactsPln;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getContactLimit() {
        return contactLimit;
    }

    public int getEmailLimitPerMonth() {
        return emailLimitPerMonth;
    }

    public int getUserLimit() {
        return userLimit;
    }

    public int getCampaignLimit() {
        return campaignLimit;
    }

    public int getAutomationLimit() {
        return automationLimit;
    }

    public double getBasePricePln() {
        return basePricePln;
    }

    public double getPricePer1000ContactsPln() {
        return pricePer1000ContactsPln;
    }

    public boolean isUnlimitedContacts() {
        return contactLimit == -1;
    }

    public boolean isUnlimitedEmails() {
        return emailLimitPerMonth == -1;
    }

    public boolean isUnlimitedUsers() {
        return userLimit == -1;
    }

    /**
     * Calculate price based on contact tier
     *
     * @param contactTier Number of contacts (in thousands)
     * @return Total price in PLN
     */
    public double calculatePrice(int contactTier) {
        if (this == FREE) {
            return 0.0;
        }
        return basePricePln + (pricePer1000ContactsPln * (contactTier - 1));
    }
}
