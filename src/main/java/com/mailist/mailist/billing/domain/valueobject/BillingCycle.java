package com.mailist.mailist.billing.domain.valueobject;

/**
 * Billing cycle for subscriptions
 */
public enum BillingCycle {
    MONTHLY("Monthly", 1),
    QUARTERLY("Quarterly", 3),
    YEARLY("Yearly", 12);

    private final String displayName;
    private final int months;

    BillingCycle(String displayName, int months) {
        this.displayName = displayName;
        this.months = months;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMonths() {
        return months;
    }
}
