package com.mailist.mailist.billing.domain.valueobject;

/**
 * Status of a payment transaction
 */
public enum PaymentStatus {
    /**
     * Payment is pending
     */
    PENDING,

    /**
     * Payment requires additional action (e.g., 3D Secure)
     */
    REQUIRES_ACTION,

    /**
     * Payment is processing
     */
    PROCESSING,

    /**
     * Payment succeeded
     */
    SUCCEEDED,

    /**
     * Payment failed
     */
    FAILED,

    /**
     * Payment was cancelled
     */
    CANCELLED,

    /**
     * Payment was refunded
     */
    REFUNDED
}
