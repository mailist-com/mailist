package com.mailist.mailist.billing.domain.gateway;

import com.mailist.mailist.billing.domain.aggregate.OrganizationSubscription;
import com.mailist.mailist.billing.domain.aggregate.Payment;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Payment provider gateway interface (Strategy Pattern)
 * This interface defines the contract for all payment providers (Stripe, PayPal, etc.)
 */
public interface PaymentProvider {

    /**
     * Get provider name
     */
    String getProviderName();

    /**
     * Create a customer in the payment provider
     *
     * @param tenantId Organization tenant ID
     * @param email Customer email
     * @param name Customer name
     * @param metadata Additional metadata
     * @return External customer ID
     */
    String createCustomer(Long tenantId, String email, String name, Map<String, String> metadata);

    /**
     * Create a subscription in the payment provider
     *
     * @param request Subscription creation request
     * @return Subscription creation response
     */
    SubscriptionCreationResponse createSubscription(SubscriptionCreationRequest request);

    /**
     * Update an existing subscription
     *
     * @param externalSubscriptionId External subscription ID
     * @param request Subscription update request
     * @return Updated subscription response
     */
    SubscriptionCreationResponse updateSubscription(String externalSubscriptionId, SubscriptionUpdateRequest request);

    /**
     * Cancel a subscription
     *
     * @param externalSubscriptionId External subscription ID
     * @param immediately Cancel immediately or at period end
     * @return Cancellation result
     */
    boolean cancelSubscription(String externalSubscriptionId, boolean immediately);

    /**
     * Create a one-time payment
     *
     * @param request Payment creation request
     * @return Payment creation response
     */
    PaymentCreationResponse createPayment(PaymentCreationRequest request);

    /**
     * Get payment status
     *
     * @param externalPaymentId External payment ID
     * @return Payment status response
     */
    PaymentStatusResponse getPaymentStatus(String externalPaymentId);

    /**
     * Refund a payment
     *
     * @param externalPaymentId External payment ID
     * @param amount Amount to refund (null for full refund)
     * @return Refund result
     */
    boolean refundPayment(String externalPaymentId, BigDecimal amount);

    /**
     * Verify webhook signature
     *
     * @param payload Webhook payload
     * @param signature Webhook signature
     * @param secret Webhook secret
     * @return true if signature is valid
     */
    boolean verifyWebhookSignature(String payload, String signature, String secret);

    /**
     * Parse webhook event
     *
     * @param payload Webhook payload
     * @return Parsed webhook event
     */
    WebhookEvent parseWebhookEvent(String payload);

    /**
     * Get checkout URL for payment
     *
     * @param request Checkout creation request
     * @return Checkout URL
     */
    String createCheckoutSession(CheckoutCreationRequest request);

    /**
     * Subscription creation request DTO
     */
    record SubscriptionCreationRequest(
        String customerId,
        Long tenantId,
        String planId,
        BigDecimal amount,
        String currency,
        String billingCycle,
        Map<String, String> metadata
    ) {}

    /**
     * Subscription update request DTO
     */
    record SubscriptionUpdateRequest(
        String newPlanId,
        BigDecimal newAmount,
        Map<String, String> metadata
    ) {}

    /**
     * Subscription creation response DTO
     */
    record SubscriptionCreationResponse(
        boolean success,
        String externalSubscriptionId,
        String externalCustomerId,
        String status,
        String errorMessage
    ) {}

    /**
     * Payment creation request DTO
     */
    record PaymentCreationRequest(
        String customerId,
        Long tenantId,
        BigDecimal amount,
        String currency,
        String description,
        Map<String, String> metadata
    ) {}

    /**
     * Payment creation response DTO
     */
    record PaymentCreationResponse(
        boolean success,
        String externalPaymentId,
        String status,
        String clientSecret,
        String errorMessage
    ) {}

    /**
     * Payment status response DTO
     */
    record PaymentStatusResponse(
        String paymentId,
        String status,
        BigDecimal amount,
        String currency,
        boolean isPaid
    ) {}

    /**
     * Webhook event DTO
     */
    record WebhookEvent(
        String eventType,
        String objectId,
        String objectType,
        Map<String, Object> data
    ) {}

    /**
     * Checkout creation request DTO
     */
    record CheckoutCreationRequest(
        String customerId,
        Long tenantId,
        BigDecimal amount,
        String currency,
        String successUrl,
        String cancelUrl,
        Map<String, String> metadata
    ) {}
}
