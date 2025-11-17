package com.mailist.mailist.billing.domain.aggregate;

import com.mailist.mailist.billing.domain.valueobject.PaymentStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Payment aggregate - represents a payment transaction
 */
@Entity
@Table(name = "payments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tenant/Organization ID
     */
    @Column(nullable = false)
    private Long tenantId;

    /**
     * Associated subscription
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private OrganizationSubscription subscription;

    /**
     * Payment amount in PLN
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPln;

    /**
     * Currency code
     */
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "PLN";

    /**
     * Payment status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * Payment method (card, bank_transfer, etc.)
     */
    @Column(length = 50)
    private String paymentMethod;

    /**
     * Payment provider (stripe, paypal, etc.)
     */
    @Column(nullable = false, length = 50)
    private String paymentProvider;

    /**
     * External payment ID from provider
     */
    @Column(length = 255)
    private String externalPaymentId;

    /**
     * External invoice ID from provider
     */
    @Column(length = 255)
    private String externalInvoiceId;

    /**
     * Payment description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Additional metadata as JSON
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Payment successful timestamp
     */
    private LocalDateTime paidAt;

    /**
     * Payment failed timestamp
     */
    private LocalDateTime failedAt;

    /**
     * Failure reason
     */
    @Column(columnDefinition = "TEXT")
    private String failureReason;

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
     * Mark payment as succeeded
     */
    public void markSucceeded() {
        this.status = PaymentStatus.SUCCEEDED;
        this.paidAt = LocalDateTime.now();
        this.failedAt = null;
        this.failureReason = null;
    }

    /**
     * Mark payment as failed
     */
    public void markFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
    }

    /**
     * Mark payment as processing
     */
    public void markProcessing() {
        this.status = PaymentStatus.PROCESSING;
    }

    /**
     * Mark payment as requiring action
     */
    public void markRequiresAction() {
        this.status = PaymentStatus.REQUIRES_ACTION;
    }

    /**
     * Mark payment as cancelled
     */
    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }

    /**
     * Mark payment as refunded
     */
    public void refund() {
        this.status = PaymentStatus.REFUNDED;
    }

    /**
     * Check if payment is successful
     */
    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCEEDED;
    }

    /**
     * Check if payment is pending or processing
     */
    public boolean isPending() {
        return status == PaymentStatus.PENDING
                || status == PaymentStatus.PROCESSING
                || status == PaymentStatus.REQUIRES_ACTION;
    }

    /**
     * Check if payment failed
     */
    public boolean isFailed() {
        return status == PaymentStatus.FAILED;
    }

    /**
     * Add metadata
     */
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }
}
