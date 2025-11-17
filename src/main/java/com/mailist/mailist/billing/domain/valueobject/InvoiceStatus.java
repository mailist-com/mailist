package com.mailist.mailist.billing.domain.valueobject;

/**
 * Status of an invoice
 */
public enum InvoiceStatus {
    /**
     * Invoice is in draft state
     */
    DRAFT,

    /**
     * Invoice has been sent to customer
     */
    SENT,

    /**
     * Invoice is due for payment
     */
    OPEN,

    /**
     * Invoice has been paid
     */
    PAID,

    /**
     * Invoice is partially paid
     */
    PARTIALLY_PAID,

    /**
     * Invoice is overdue
     */
    OVERDUE,

    /**
     * Invoice has been cancelled
     */
    CANCELLED,

    /**
     * Invoice has been voided
     */
    VOIDED
}
