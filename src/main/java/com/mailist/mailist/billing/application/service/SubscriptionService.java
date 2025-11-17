package com.mailist.mailist.billing.application.service;

import com.mailist.mailist.billing.domain.aggregate.*;
import com.mailist.mailist.billing.domain.gateway.InvoicingProvider;
import com.mailist.mailist.billing.domain.gateway.PaymentProvider;
import com.mailist.mailist.billing.domain.repository.*;
import com.mailist.mailist.billing.domain.valueobject.BillingCycle;
import com.mailist.mailist.billing.domain.valueobject.PaymentStatus;
import com.mailist.mailist.billing.domain.valueobject.SubscriptionStatus;
import com.mailist.mailist.billing.infrastructure.gateway.invoicing.InvoicingProviderFactory;
import com.mailist.mailist.billing.infrastructure.gateway.payment.PaymentProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Application service for subscription management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionService {

    private final OrganizationSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentProviderFactory paymentProviderFactory;
    private final InvoicingProviderFactory invoicingProviderFactory;

    /**
     * Create a new subscription for an organization
     */
    public OrganizationSubscription createSubscription(
            Long tenantId,
            String planName,
            int contactTier,
            BillingCycle billingCycle,
            String paymentProviderName,
            String customerEmail,
            String customerName
    ) {
        // Get subscription plan
        SubscriptionPlan plan = planRepository.findByName(planName)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planName));

        // Calculate price
        BigDecimal price = plan.calculatePrice(contactTier);

        // Get payment provider
        PaymentProvider paymentProvider = paymentProviderFactory.getProvider(paymentProviderName);

        // Create customer in payment provider
        Map<String, String> metadata = new HashMap<>();
        metadata.put("tenant_id", String.valueOf(tenantId));
        metadata.put("plan", planName);

        String externalCustomerId = paymentProvider.createCustomer(
                tenantId,
                customerEmail,
                customerName,
                metadata
        );

        // Create subscription in payment provider
        PaymentProvider.SubscriptionCreationRequest subscriptionRequest =
                new PaymentProvider.SubscriptionCreationRequest(
                        externalCustomerId,
                        tenantId,
                        planName,
                        price,
                        "PLN",
                        billingCycle.name(),
                        metadata
                );

        PaymentProvider.SubscriptionCreationResponse response =
                paymentProvider.createSubscription(subscriptionRequest);

        if (!response.success()) {
            throw new RuntimeException("Failed to create subscription: " + response.errorMessage());
        }

        // Create subscription record
        OrganizationSubscription subscription = OrganizationSubscription.builder()
                .tenantId(tenantId)
                .subscriptionPlan(plan)
                .contactTier(contactTier)
                .pricePln(price)
                .status(SubscriptionStatus.ACTIVE)
                .billingCycle(billingCycle)
                .startDate(LocalDateTime.now())
                .paymentProvider(paymentProviderName)
                .externalSubscriptionId(response.externalSubscriptionId())
                .externalCustomerId(response.externalCustomerId())
                .build();

        subscription = subscriptionRepository.save(subscription);
        log.info("Created subscription {} for tenant {}", subscription.getId(), tenantId);

        return subscription;
    }

    /**
     * Upgrade or downgrade subscription
     */
    public OrganizationSubscription changeSubscription(
            Long tenantId,
            String newPlanName,
            int newContactTier
    ) {
        OrganizationSubscription subscription = subscriptionRepository
                .findActiveByTenantId(tenantId, LocalDateTime.now())
                .orElseThrow(() -> new IllegalStateException("No active subscription found"));

        SubscriptionPlan newPlan = planRepository.findByName(newPlanName)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + newPlanName));

        // Calculate new price
        BigDecimal newPrice = newPlan.calculatePrice(newContactTier);

        // Update subscription in payment provider
        PaymentProvider paymentProvider = paymentProviderFactory.getProvider(subscription.getPaymentProvider());

        Map<String, String> metadata = new HashMap<>();
        metadata.put("new_plan", newPlanName);
        metadata.put("new_tier", String.valueOf(newContactTier));

        PaymentProvider.SubscriptionUpdateRequest updateRequest =
                new PaymentProvider.SubscriptionUpdateRequest(
                        newPlanName,
                        newPrice,
                        metadata
                );

        PaymentProvider.SubscriptionCreationResponse response =
                paymentProvider.updateSubscription(subscription.getExternalSubscriptionId(), updateRequest);

        if (!response.success()) {
            throw new RuntimeException("Failed to update subscription: " + response.errorMessage());
        }

        // Update local subscription
        subscription.changePlan(newPlan, newContactTier);
        subscription = subscriptionRepository.save(subscription);

        log.info("Updated subscription {} for tenant {} to plan {} with tier {}",
                subscription.getId(), tenantId, newPlanName, newContactTier);

        return subscription;
    }

    /**
     * Cancel subscription
     */
    public void cancelSubscription(Long tenantId, String reason, boolean immediately) {
        OrganizationSubscription subscription = subscriptionRepository
                .findActiveByTenantId(tenantId, LocalDateTime.now())
                .orElseThrow(() -> new IllegalStateException("No active subscription found"));

        // Cancel in payment provider
        PaymentProvider paymentProvider = paymentProviderFactory.getProvider(subscription.getPaymentProvider());
        boolean cancelled = paymentProvider.cancelSubscription(
                subscription.getExternalSubscriptionId(),
                immediately
        );

        if (!cancelled) {
            throw new RuntimeException("Failed to cancel subscription in payment provider");
        }

        // Update local subscription
        subscription.cancel(reason);
        subscriptionRepository.save(subscription);

        log.info("Cancelled subscription {} for tenant {}: {}",
                subscription.getId(), tenantId, reason);
    }

    /**
     * Process successful payment and generate invoice
     */
    public void processSuccessfulPayment(
            String externalPaymentId,
            String externalSubscriptionId,
            BigDecimal amount,
            String buyerEmail
    ) {
        // Find or create payment record
        Payment payment = paymentRepository.findByExternalPaymentId(externalPaymentId)
                .orElseGet(() -> {
                    OrganizationSubscription subscription = subscriptionRepository
                            .findByExternalSubscriptionId(externalSubscriptionId)
                            .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

                    return Payment.builder()
                            .tenantId(subscription.getTenantId())
                            .subscription(subscription)
                            .amountPln(amount)
                            .currency("PLN")
                            .paymentProvider(subscription.getPaymentProvider())
                            .externalPaymentId(externalPaymentId)
                            .build();
                });

        payment.markSucceeded();
        payment = paymentRepository.save(payment);

        log.info("Processed successful payment: {}", payment.getId());

        // Generate invoice using Fakturownia
        generateInvoice(payment, buyerEmail);

        // Update subscription next billing date
        if (payment.getSubscription() != null) {
            OrganizationSubscription subscription = payment.getSubscription();
            subscription.updateNextBillingDate();
            subscriptionRepository.save(subscription);
        }
    }

    /**
     * Generate invoice for payment
     */
    private void generateInvoice(Payment payment, String buyerEmail) {
        try {
            InvoicingProvider invoicingProvider = invoicingProviderFactory.getProvider("fakturownia");

            // Prepare invoice data
            BigDecimal netAmount = payment.getAmountPln();
            BigDecimal taxRate = BigDecimal.valueOf(0.23); // 23% VAT
            BigDecimal taxAmount = netAmount.multiply(taxRate);
            BigDecimal totalAmount = netAmount.add(taxAmount);

            String description = "Subskrypcja Mailist";
            if (payment.getSubscription() != null) {
                description += " - " + payment.getSubscription().getSubscriptionPlan().getDisplayName();
            }

            InvoicingProvider.InvoiceItem item = new InvoicingProvider.InvoiceItem(
                    "Subskrypcja Mailist",
                    description,
                    BigDecimal.ONE,
                    netAmount,
                    "usługa",
                    taxRate,
                    totalAmount
            );

            InvoicingProvider.BuyerInfo buyer = new InvoicingProvider.BuyerInfo(
                    "Klient",
                    buyerEmail,
                    null,
                    null,
                    null,
                    null,
                    "PL"
            );

            InvoicingProvider.InvoiceCreationRequest invoiceRequest =
                    new InvoicingProvider.InvoiceCreationRequest(
                            payment.getTenantId(),
                            null,
                            java.time.LocalDate.now(),
                            java.time.LocalDate.now().plusDays(14),
                            "vat",
                            List.of(item),
                            buyer,
                            Map.of("payment_id", String.valueOf(payment.getId()))
                    );

            InvoicingProvider.InvoiceCreationResponse invoiceResponse =
                    invoicingProvider.createInvoice(invoiceRequest);

            if (invoiceResponse.success()) {
                // Create invoice record
                Invoice invoice = Invoice.builder()
                        .tenantId(payment.getTenantId())
                        .payment(payment)
                        .subscription(payment.getSubscription())
                        .invoiceNumber(invoiceResponse.invoiceNumber())
                        .externalInvoiceId(invoiceResponse.externalInvoiceId())
                        .amountPln(netAmount)
                        .taxAmountPln(taxAmount)
                        .totalAmountPln(totalAmount)
                        .currency("PLN")
                        .status(com.mailist.mailist.billing.domain.valueobject.InvoiceStatus.PAID)
                        .invoiceType("vat")
                        .invoiceDate(java.time.LocalDate.now())
                        .dueDate(java.time.LocalDate.now().plusDays(14))
                        .paidDate(java.time.LocalDate.now())
                        .pdfUrl(invoiceResponse.pdfUrl())
                        .fakturowniaUrl(invoiceResponse.invoiceUrl())
                        .buyerEmail(buyerEmail)
                        .build();

                invoiceRepository.save(invoice);
                log.info("Generated invoice {} for payment {}", invoice.getInvoiceNumber(), payment.getId());

                // Send invoice by email
                invoicingProvider.sendInvoiceByEmail(invoiceResponse.externalInvoiceId());
            } else {
                log.error("Failed to generate invoice: {}", invoiceResponse.errorMessage());
            }

        } catch (Exception e) {
            log.error("Error generating invoice for payment {}", payment.getId(), e);
        }
    }

    /**
     * Get active subscription for tenant
     */
    @Transactional(readOnly = true)
    public OrganizationSubscription getActiveSubscription(Long tenantId) {
        return subscriptionRepository.findActiveByTenantId(tenantId, LocalDateTime.now())
                .orElse(null);
    }

    /**
     * Get subscription history for tenant
     */
    @Transactional(readOnly = true)
    public List<OrganizationSubscription> getSubscriptionHistory(Long tenantId) {
        return subscriptionRepository.findAllByTenantId(tenantId);
    }

    /**
     * Get payment history for tenant
     */
    @Transactional(readOnly = true)
    public List<Payment> getPaymentHistory(Long tenantId) {
        return paymentRepository.findSuccessfulPaymentsByTenantId(tenantId);
    }

    /**
     * Get invoices for tenant
     */
    @Transactional(readOnly = true)
    public List<Invoice> getInvoices(Long tenantId) {
        return invoiceRepository.findPaidInvoicesByTenantId(tenantId);
    }
}
