package com.mailist.mailist.billing.domain.gateway;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Invoicing provider gateway interface (Strategy Pattern)
 * This interface defines the contract for all invoicing providers (Fakturownia, InFakt, etc.)
 */
public interface InvoicingProvider {

    /**
     * Get provider name
     */
    String getProviderName();

    /**
     * Create an invoice
     *
     * @param request Invoice creation request
     * @return Invoice creation response
     */
    InvoiceCreationResponse createInvoice(InvoiceCreationRequest request);

    /**
     * Get invoice details
     *
     * @param externalInvoiceId External invoice ID
     * @return Invoice details
     */
    InvoiceDetailsResponse getInvoice(String externalInvoiceId);

    /**
     * Mark invoice as paid
     *
     * @param externalInvoiceId External invoice ID
     * @param paidDate Payment date
     * @return Success status
     */
    boolean markInvoiceAsPaid(String externalInvoiceId, LocalDate paidDate);

    /**
     * Cancel/void an invoice
     *
     * @param externalInvoiceId External invoice ID
     * @return Success status
     */
    boolean cancelInvoice(String externalInvoiceId);

    /**
     * Send invoice to customer via email
     *
     * @param externalInvoiceId External invoice ID
     * @return Success status
     */
    boolean sendInvoiceByEmail(String externalInvoiceId);

    /**
     * Get invoice PDF URL
     *
     * @param externalInvoiceId External invoice ID
     * @return PDF download URL
     */
    String getInvoicePdfUrl(String externalInvoiceId);

    /**
     * Create or update customer in invoicing system
     *
     * @param request Customer creation request
     * @return External customer ID
     */
    String createOrUpdateCustomer(CustomerCreationRequest request);

    /**
     * Invoice creation request DTO
     */
    record InvoiceCreationRequest(
        Long tenantId,
        String externalCustomerId,
        LocalDate issueDate,
        LocalDate dueDate,
        String invoiceType,
        List<InvoiceItem> items,
        BuyerInfo buyer,
        Map<String, String> metadata
    ) {}

    /**
     * Invoice item DTO
     */
    record InvoiceItem(
        String name,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        String unit,
        BigDecimal taxRate,
        BigDecimal totalPrice
    ) {}

    /**
     * Buyer information DTO
     */
    record BuyerInfo(
        String name,
        String email,
        String taxId,
        String street,
        String city,
        String postalCode,
        String country
    ) {}

    /**
     * Invoice creation response DTO
     */
    record InvoiceCreationResponse(
        boolean success,
        String externalInvoiceId,
        String invoiceNumber,
        String pdfUrl,
        String invoiceUrl,
        String errorMessage
    ) {}

    /**
     * Invoice details response DTO
     */
    record InvoiceDetailsResponse(
        String invoiceId,
        String invoiceNumber,
        String status,
        BigDecimal totalAmount,
        String currency,
        LocalDate issueDate,
        LocalDate dueDate,
        LocalDate paidDate,
        String pdfUrl
    ) {}

    /**
     * Customer creation request DTO
     */
    record CustomerCreationRequest(
        Long tenantId,
        String name,
        String email,
        String taxId,
        String street,
        String city,
        String postalCode,
        String country
    ) {}
}
