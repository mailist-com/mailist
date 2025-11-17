package com.mailist.mailist.billing.domain.valueobject;

/**
 * Status of a subscription
 */
public enum SubscriptionStatus {
    /**
     * Subscription is active and in good standing
     */
    ACTIVE,

    /**
     * Subscription is in trial period
     */
    TRIALING,

    /**
     * Subscription is past due (payment failed)
     */
    PAST_DUE,

    /**
     * Subscription has been cancelled but still active until end date
     */
    CANCELLED,

    /**
     * Subscription has expired
     */
    EXPIRED,

    /**
     * Subscription is suspended (e.g., limits exceeded)
     */
    SUSPENDED,

    /**
     * Subscription is incomplete (payment not completed)
     */
    INCOMPLETE
}
