package com.mailist.mailist.billing.interfaces.controller;

import com.mailist.mailist.billing.application.service.SubscriptionService;
import com.mailist.mailist.billing.domain.gateway.PaymentProvider;
import com.mailist.mailist.billing.infrastructure.gateway.payment.PaymentProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Webhook handler for Stripe events
 */
@RestController
@RequestMapping("/api/webhooks/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final PaymentProviderFactory paymentProviderFactory;
    private final SubscriptionService subscriptionService;

    @Value("${billing.stripe.webhook-secret}")
    private String webhookSecret;

    /**
     * Handle Stripe webhook events
     */
    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        log.info("Received Stripe webhook");

        try {
            PaymentProvider stripeProvider = paymentProviderFactory.getProvider("stripe");

            // Verify webhook signature
            if (!stripeProvider.verifyWebhookSignature(payload, signature, webhookSecret)) {
                log.error("Invalid Stripe webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid signature");
            }

            // Parse webhook event
            PaymentProvider.WebhookEvent event = stripeProvider.parseWebhookEvent(payload);
            log.info("Processing Stripe event: {}", event.eventType());

            // Handle different event types
            switch (event.eventType()) {
                case "payment_intent.succeeded":
                    handlePaymentSucceeded(event);
                    break;

                case "payment_intent.payment_failed":
                    handlePaymentFailed(event);
                    break;

                case "customer.subscription.created":
                    handleSubscriptionCreated(event);
                    break;

                case "customer.subscription.updated":
                    handleSubscriptionUpdated(event);
                    break;

                case "customer.subscription.deleted":
                    handleSubscriptionDeleted(event);
                    break;

                case "invoice.paid":
                    handleInvoicePaid(event);
                    break;

                case "invoice.payment_failed":
                    handleInvoicePaymentFailed(event);
                    break;

                default:
                    log.info("Unhandled Stripe event type: {}", event.eventType());
            }

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            log.error("Error processing Stripe webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook: " + e.getMessage());
        }
    }

    /**
     * Handle successful payment
     */
    private void handlePaymentSucceeded(PaymentProvider.WebhookEvent event) {
        log.info("Processing payment success for: {}", event.objectId());

        try {
            Map<String, Object> data = event.data();
            Object paymentIntentObj = data.get("object");

            if (paymentIntentObj instanceof com.stripe.model.PaymentIntent) {
                com.stripe.model.PaymentIntent paymentIntent =
                        (com.stripe.model.PaymentIntent) paymentIntentObj;

                BigDecimal amount = BigDecimal.valueOf(paymentIntent.getAmount())
                        .divide(BigDecimal.valueOf(100));

                String subscriptionId = paymentIntent.getMetadata().get("subscription_id");
                String customerEmail = paymentIntent.getMetadata().get("customer_email");

                if (subscriptionId != null) {
                    subscriptionService.processSuccessfulPayment(
                            paymentIntent.getId(),
                            subscriptionId,
                            amount,
                            customerEmail
                    );

                    log.info("Successfully processed payment: {}", paymentIntent.getId());
                }
            }

        } catch (Exception e) {
            log.error("Error handling payment success", e);
        }
    }

    /**
     * Handle failed payment
     */
    private void handlePaymentFailed(PaymentProvider.WebhookEvent event) {
        log.warn("Payment failed for: {}", event.objectId());
        // TODO: Implement payment failure handling
        // - Send notification to user
        // - Mark subscription as past_due
        // - Implement retry logic
    }

    /**
     * Handle subscription created
     */
    private void handleSubscriptionCreated(PaymentProvider.WebhookEvent event) {
        log.info("Subscription created: {}", event.objectId());
        // Subscription is already created via API, this is just confirmation
    }

    /**
     * Handle subscription updated
     */
    private void handleSubscriptionUpdated(PaymentProvider.WebhookEvent event) {
        log.info("Subscription updated: {}", event.objectId());
        // TODO: Sync subscription changes from Stripe
    }

    /**
     * Handle subscription deleted/cancelled
     */
    private void handleSubscriptionDeleted(PaymentProvider.WebhookEvent event) {
        log.info("Subscription deleted: {}", event.objectId());
        // TODO: Mark subscription as cancelled in database
    }

    /**
     * Handle invoice paid
     */
    private void handleInvoicePaid(PaymentProvider.WebhookEvent event) {
        log.info("Invoice paid: {}", event.objectId());

        try {
            Map<String, Object> data = event.data();
            Object invoiceObj = data.get("object");

            if (invoiceObj instanceof com.stripe.model.Invoice) {
                com.stripe.model.Invoice stripeInvoice = (com.stripe.model.Invoice) invoiceObj;

                BigDecimal amount = BigDecimal.valueOf(stripeInvoice.getAmountPaid())
                        .divide(BigDecimal.valueOf(100));

                String subscriptionId = stripeInvoice.getSubscription();
                String customerEmail = stripeInvoice.getCustomerEmail();

                if (subscriptionId != null) {
                    subscriptionService.processSuccessfulPayment(
                            stripeInvoice.getPaymentIntent(),
                            subscriptionId,
                            amount,
                            customerEmail
                    );

                    log.info("Successfully processed invoice payment: {}", stripeInvoice.getId());
                }
            }

        } catch (Exception e) {
            log.error("Error handling invoice paid", e);
        }
    }

    /**
     * Handle invoice payment failed
     */
    private void handleInvoicePaymentFailed(PaymentProvider.WebhookEvent event) {
        log.warn("Invoice payment failed: {}", event.objectId());
        // TODO: Implement invoice payment failure handling
        // - Send notification to user
        // - Update subscription status
    }
}
