package com.mailist.mailist.billing.infrastructure.gateway.payment;

import com.mailist.mailist.billing.domain.gateway.PaymentProvider;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.*;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Stripe payment provider implementation
 * Handles all Stripe API interactions for payments and subscriptions
 */
@Component
@Slf4j
public class StripePaymentProvider implements PaymentProvider {

    @Value("${billing.stripe.api-key}")
    private String apiKey;

    @Value("${billing.stripe.webhook-secret:}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
        log.info("Stripe payment provider initialized");
    }

    @Override
    public String getProviderName() {
        return "stripe";
    }

    @Override
    public String createCustomer(Long tenantId, String email, String name, Map<String, String> metadata) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("email", email);
            params.put("name", name);
            params.put("description", "Tenant: " + tenantId);

            Map<String, String> customerMetadata = new HashMap<>(metadata);
            customerMetadata.put("tenant_id", String.valueOf(tenantId));
            params.put("metadata", customerMetadata);

            Customer customer = Customer.create(params);
            log.info("Created Stripe customer: {} for tenant: {}", customer.getId(), tenantId);
            return customer.getId();

        } catch (StripeException e) {
            log.error("Failed to create Stripe customer for tenant: {}", tenantId, e);
            throw new RuntimeException("Failed to create Stripe customer: " + e.getMessage(), e);
        }
    }

    @Override
    public SubscriptionCreationResponse createSubscription(SubscriptionCreationRequest request) {
        try {
            // Create a Stripe Price dynamically based on amount
            String priceId = createDynamicPrice(
                request.amount(),
                request.currency(),
                request.billingCycle(),
                request.metadata()
            );

            // Create subscription parameters
            SubscriptionCreateParams.Builder paramsBuilder = SubscriptionCreateParams.builder()
                .setCustomer(request.customerId())
                .addItem(SubscriptionCreateParams.Item.builder()
                    .setPrice(priceId)
                    .build())
                .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                .setPaymentSettings(SubscriptionCreateParams.PaymentSettings.builder()
                    .setSaveDefaultPaymentMethod(SubscriptionCreateParams.PaymentSettings.SaveDefaultPaymentMethod.ON_SUBSCRIPTION)
                    .build())
                .addAllExpand(java.util.List.of("latest_invoice.payment_intent"));

            // Add metadata
            if (request.metadata() != null && !request.metadata().isEmpty()) {
                Map<String, String> subscriptionMetadata = new HashMap<>(request.metadata());
                subscriptionMetadata.put("tenant_id", String.valueOf(request.tenantId()));
                paramsBuilder.putAllMetadata(subscriptionMetadata);
            }

            Subscription subscription = Subscription.create(paramsBuilder.build());

            log.info("Created Stripe subscription: {} for tenant: {}", subscription.getId(), request.tenantId());

            return new SubscriptionCreationResponse(
                true,
                subscription.getId(),
                subscription.getCustomer(),
                subscription.getStatus(),
                null
            );

        } catch (StripeException e) {
            log.error("Failed to create Stripe subscription for tenant: {}", request.tenantId(), e);
            return new SubscriptionCreationResponse(
                false,
                null,
                null,
                null,
                e.getMessage()
            );
        }
    }

    @Override
    public SubscriptionCreationResponse updateSubscription(String externalSubscriptionId, SubscriptionUpdateRequest request) {
        try {
            Subscription subscription = Subscription.retrieve(externalSubscriptionId);

            // Create new price if amount changed
            String newPriceId = null;
            if (request.newAmount() != null) {
                String billingCycle = determineBillingCycle(subscription);
                newPriceId = createDynamicPrice(
                    request.newAmount(),
                    "PLN",
                    billingCycle,
                    request.metadata()
                );
            }

            // Update subscription parameters
            SubscriptionUpdateParams.Builder paramsBuilder = SubscriptionUpdateParams.builder();

            if (newPriceId != null) {
                paramsBuilder.addItem(SubscriptionUpdateParams.Item.builder()
                    .setId(subscription.getItems().getData().get(0).getId())
                    .setPrice(newPriceId)
                    .build());
            }

            if (request.metadata() != null && !request.metadata().isEmpty()) {
                paramsBuilder.putAllMetadata(request.metadata());
            }

            Subscription updatedSubscription = subscription.update(paramsBuilder.build());

            log.info("Updated Stripe subscription: {}", externalSubscriptionId);

            return new SubscriptionCreationResponse(
                true,
                updatedSubscription.getId(),
                updatedSubscription.getCustomer(),
                updatedSubscription.getStatus(),
                null
            );

        } catch (StripeException e) {
            log.error("Failed to update Stripe subscription: {}", externalSubscriptionId, e);
            return new SubscriptionCreationResponse(
                false,
                null,
                null,
                null,
                e.getMessage()
            );
        }
    }

    @Override
    public boolean cancelSubscription(String externalSubscriptionId, boolean immediately) {
        try {
            Subscription subscription = Subscription.retrieve(externalSubscriptionId);

            if (immediately) {
                subscription.cancel();
                log.info("Immediately cancelled Stripe subscription: {}", externalSubscriptionId);
            } else {
                SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true)
                    .build();
                subscription.update(params);
                log.info("Scheduled cancellation at period end for Stripe subscription: {}", externalSubscriptionId);
            }

            return true;

        } catch (StripeException e) {
            log.error("Failed to cancel Stripe subscription: {}", externalSubscriptionId, e);
            return false;
        }
    }

    @Override
    public PaymentCreationResponse createPayment(PaymentCreationRequest request) {
        try {
            // Convert amount to cents (Stripe uses smallest currency unit)
            long amountInCents = request.amount().multiply(BigDecimal.valueOf(100)).longValue();

            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(request.currency().toLowerCase())
                .setCustomer(request.customerId())
                .setDescription(request.description())
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                );

            // Add metadata
            if (request.metadata() != null && !request.metadata().isEmpty()) {
                Map<String, String> paymentMetadata = new HashMap<>(request.metadata());
                paymentMetadata.put("tenant_id", String.valueOf(request.tenantId()));
                paramsBuilder.putAllMetadata(paymentMetadata);
            }

            PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build());

            log.info("Created Stripe payment intent: {} for tenant: {}", paymentIntent.getId(), request.tenantId());

            return new PaymentCreationResponse(
                true,
                paymentIntent.getId(),
                paymentIntent.getStatus(),
                paymentIntent.getClientSecret(),
                null
            );

        } catch (StripeException e) {
            log.error("Failed to create Stripe payment for tenant: {}", request.tenantId(), e);
            return new PaymentCreationResponse(
                false,
                null,
                null,
                null,
                e.getMessage()
            );
        }
    }

    @Override
    public PaymentStatusResponse getPaymentStatus(String externalPaymentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(externalPaymentId);

            BigDecimal amount = BigDecimal.valueOf(paymentIntent.getAmount())
                .divide(BigDecimal.valueOf(100));

            return new PaymentStatusResponse(
                paymentIntent.getId(),
                paymentIntent.getStatus(),
                amount,
                paymentIntent.getCurrency().toUpperCase(),
                "succeeded".equals(paymentIntent.getStatus())
            );

        } catch (StripeException e) {
            log.error("Failed to retrieve Stripe payment status: {}", externalPaymentId, e);
            return new PaymentStatusResponse(
                externalPaymentId,
                "unknown",
                BigDecimal.ZERO,
                "PLN",
                false
            );
        }
    }

    @Override
    public boolean refundPayment(String externalPaymentId, BigDecimal amount) {
        try {
            RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                .setPaymentIntent(externalPaymentId);

            if (amount != null) {
                long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();
                paramsBuilder.setAmount(amountInCents);
            }

            Refund refund = Refund.create(paramsBuilder.build());
            log.info("Created Stripe refund: {} for payment: {}", refund.getId(), externalPaymentId);
            return true;

        } catch (StripeException e) {
            log.error("Failed to refund Stripe payment: {}", externalPaymentId, e);
            return false;
        }
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signature, String secret) {
        try {
            String webhookSecretToUse = (secret != null && !secret.isEmpty()) ? secret : webhookSecret;
            Webhook.constructEvent(payload, signature, webhookSecretToUse);
            return true;
        } catch (Exception e) {
            log.error("Failed to verify Stripe webhook signature", e);
            return false;
        }
    }

    @Override
    public WebhookEvent parseWebhookEvent(String payload) {
        try {
            Event event = Event.GSON.fromJson(payload, Event.class);

            Map<String, Object> data = new HashMap<>();
            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                StripeObject stripeObject = event.getDataObjectDeserializer().getObject().get();
                data.put("object", stripeObject);
            }

            return new WebhookEvent(
                event.getType(),
                event.getData().getObject().get("id").toString(),
                event.getData().getObject().get("object").toString(),
                data
            );

        } catch (Exception e) {
            log.error("Failed to parse Stripe webhook event", e);
            throw new RuntimeException("Failed to parse webhook event", e);
        }
    }

    @Override
    public String createCheckoutSession(CheckoutCreationRequest request) {
        try {
            // Convert amount to cents
            long amountInCents = request.amount().multiply(BigDecimal.valueOf(100)).longValue();

            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(request.successUrl())
                .setCancelUrl(request.cancelUrl())
                .addLineItem(SessionCreateParams.LineItem.builder()
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(request.currency().toLowerCase())
                        .setUnitAmount(amountInCents)
                        .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName("Mailist Subscription")
                            .build())
                        .build())
                    .setQuantity(1L)
                    .build());

            if (request.customerId() != null) {
                paramsBuilder.setCustomer(request.customerId());
            }

            if (request.metadata() != null && !request.metadata().isEmpty()) {
                Map<String, String> sessionMetadata = new HashMap<>(request.metadata());
                sessionMetadata.put("tenant_id", String.valueOf(request.tenantId()));
                paramsBuilder.putAllMetadata(sessionMetadata);
            }

            Session session = Session.create(paramsBuilder.build());
            log.info("Created Stripe checkout session: {} for tenant: {}", session.getId(), request.tenantId());

            return session.getUrl();

        } catch (StripeException e) {
            log.error("Failed to create Stripe checkout session for tenant: {}", request.tenantId(), e);
            throw new RuntimeException("Failed to create checkout session: " + e.getMessage(), e);
        }
    }

    /**
     * Create a dynamic price in Stripe based on amount
     */
    private String createDynamicPrice(BigDecimal amount, String currency, String billingCycle, Map<String, String> metadata) throws StripeException {
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        PriceCreateParams.Builder priceBuilder = PriceCreateParams.builder()
            .setCurrency(currency.toLowerCase())
            .setUnitAmount(amountInCents)
            .setProductData(PriceCreateParams.ProductData.builder()
                .setName("Mailist Subscription - " + amount + " " + currency)
                .build());

        // Set recurring interval based on billing cycle
        PriceCreateParams.Recurring.Interval interval = switch (billingCycle.toUpperCase()) {
            case "QUARTERLY" -> PriceCreateParams.Recurring.Interval.MONTH;
            case "YEARLY" -> PriceCreateParams.Recurring.Interval.YEAR;
            default -> PriceCreateParams.Recurring.Interval.MONTH;
        };

        int intervalCount = billingCycle.toUpperCase().equals("QUARTERLY") ? 3 : 1;

        priceBuilder.setRecurring(PriceCreateParams.Recurring.builder()
            .setInterval(interval)
            .setIntervalCount((long) intervalCount)
            .build());

        if (metadata != null && !metadata.isEmpty()) {
            priceBuilder.putAllMetadata(metadata);
        }

        Price price = Price.create(priceBuilder.build());
        log.info("Created dynamic Stripe price: {} for amount: {} {}", price.getId(), amount, currency);

        return price.getId();
    }

    /**
     * Determine billing cycle from subscription
     */
    private String determineBillingCycle(Subscription subscription) {
        if (subscription.getItems() != null && !subscription.getItems().getData().isEmpty()) {
            try {
                Price price = subscription.getItems().getData().get(0).getPrice();
                if (price.getRecurring() != null) {
                    String interval = price.getRecurring().getInterval();
                    long intervalCount = price.getRecurring().getIntervalCount();

                    if ("month".equals(interval) && intervalCount == 3) {
                        return "QUARTERLY";
                    } else if ("year".equals(interval)) {
                        return "YEARLY";
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to determine billing cycle, defaulting to MONTHLY", e);
            }
        }
        return "MONTHLY";
    }
}
