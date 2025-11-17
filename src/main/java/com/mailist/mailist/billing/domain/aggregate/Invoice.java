package com.mailist.mailist.billing.domain.aggregate;

import com.mailist.mailist.billing.domain.valueobject.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Invoice aggregate - represents an invoice from Fakturownia
 */
@Entity
@Table(name = "invoices")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tenant/Organization ID
     */
    @Column(nullable = false)
    private Long tenantId;

    /**
     * Associated payment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    /**
     * Associated subscription
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private OrganizationSubscription subscription;

    /**
     * Invoice number (from Fakturownia)
     */
    @Column(nullable = false, unique = true, length = 100)
    private String invoiceNumber;

    /**
     * External invoice ID from Fakturownia
     */
    @Column(length = 255)
    private String externalInvoiceId;

    /**
     * Net amount in PLN
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPln;

    /**
     * Tax amount in PLN (VAT)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmountPln;

    /**
     * Total amount including tax
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmountPln;

    /**
     * Currency code
     */
    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "PLN";

    /**
     * Invoice status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    /**
     * Invoice type (proforma, vat, etc.)
     */
    @Column(nullable = false, length = 50)
    private String invoiceType;

    /**
     * Invoice issue date
     */
    @Column(nullable = false)
    private LocalDate invoiceDate;

    /**
     * Payment due date
     */
    @Column(nullable = false)
    private LocalDate dueDate;

    /**
     * Date when invoice was paid
     */
    private LocalDate paidDate;

    /**
     * PDF download URL
     */
    @Column(columnDefinition = "TEXT")
    private String pdfUrl;

    /**
     * Fakturownia web URL
     */
    @Column(columnDefinition = "TEXT")
    private String fakturowniaUrl;

    /**
     * Invoice description/notes
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    // Buyer information
    @Column(length = 255)
    private String buyerName;

    @Column(length = 255)
    private String buyerEmail;

    @Column(length = 50)
    private String buyerTaxId;

    @Column(columnDefinition = "TEXT")
    private String buyerAddress;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Set default dates
        if (invoiceDate == null) {
            invoiceDate = LocalDate.now();
        }
        if (dueDate == null) {
            dueDate = invoiceDate.plusDays(14); // Default 14 days payment term
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Mark invoice as paid
     */
    public void markPaid() {
        this.status = InvoiceStatus.PAID;
        this.paidDate = LocalDate.now();
    }

    /**
     * Mark invoice as sent
     */
    public void markSent() {
        if (this.status == InvoiceStatus.DRAFT) {
            this.status = InvoiceStatus.SENT;
        }
    }

    /**
     * Mark invoice as open
     */
    public void markOpen() {
        this.status = InvoiceStatus.OPEN;
    }

    /**
     * Mark invoice as overdue
     */
    public void markOverdue() {
        this.status = InvoiceStatus.OVERDUE;
    }

    /**
     * Cancel invoice
     */
    public void cancel() {
        this.status = InvoiceStatus.CANCELLED;
    }

    /**
     * Void invoice
     */
    public void voidInvoice() {
        this.status = InvoiceStatus.VOIDED;
    }

    /**
     * Check if invoice is paid
     */
    public boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }

    /**
     * Check if invoice is overdue
     */
    public boolean isOverdue() {
        return status == InvoiceStatus.OVERDUE
                || (status == InvoiceStatus.OPEN && dueDate.isBefore(LocalDate.now()));
    }

    /**
     * Calculate VAT rate
     */
    public BigDecimal getVatRate() {
        if (amountPln.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return taxAmountPln.divide(amountPln, 4, BigDecimal.ROUND_HALF_UP);
    }
}
