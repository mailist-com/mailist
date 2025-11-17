package com.mailist.mailist.billing.interfaces.controller;

import com.mailist.mailist.billing.application.service.SubscriptionLimitService;
import com.mailist.mailist.billing.application.service.SubscriptionService;
import com.mailist.mailist.billing.application.service.UsageTrackingService;
import com.mailist.mailist.billing.domain.aggregate.*;
import com.mailist.mailist.billing.domain.repository.SubscriptionPlanRepository;
import com.mailist.mailist.billing.domain.valueobject.BillingCycle;
import com.mailist.mailist.billing.interfaces.dto.SubscriptionDto;
import com.mailist.mailist.shared.infrastructure.security.SecurityUtils;
import com.mailist.mailist.shared.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API Controller for Subscription Management
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscription Management", description = "Manage subscriptions, plans, and billing")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionPlanRepository planRepository;
    private final UsageTrackingService usageTrackingService;
    private final SubscriptionLimitService subscriptionLimitService;

    /**
     * Get all available subscription plans
     */
    @GetMapping("/plans")
    @Operation(summary = "Get all available subscription plans")
    public ResponseEntity<ApiResponse<List<SubscriptionDto.SubscriptionPlanResponse>>> getPlans() {
        log.info("Getting all subscription plans");

        List<SubscriptionPlan> plans = planRepository.findAllActivePlans();

        List<SubscriptionDto.SubscriptionPlanResponse> response = plans.stream()
                .map(this::mapPlanToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get current active subscription
     */
    @GetMapping("/current")
    @Operation(summary = "Get current active subscription")
    public ResponseEntity<ApiResponse<SubscriptionDto.SubscriptionResponse>> getCurrentSubscription() {
        Long tenantId = SecurityUtils.getTenantId();
        log.info("Getting current subscription for tenant: {}", tenantId);

        OrganizationSubscription subscription = subscriptionService.getActiveSubscription(tenantId);

        if (subscription == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "No active subscription"));
        }

        SubscriptionDto.SubscriptionResponse response = mapSubscriptionToResponse(subscription);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Create a new subscription
     */
    @PostMapping
    @Operation(summary = "Create a new subscription")
    public ResponseEntity<ApiResponse<SubscriptionDto.SubscriptionResponse>> createSubscription(
            @Valid @RequestBody SubscriptionDto.CreateSubscriptionRequest request) {

        Long tenantId = SecurityUtils.getTenantId();
        log.info("Creating subscription for tenant: {}, plan: {}", tenantId, request.getPlanName());

        try {
            OrganizationSubscription subscription = subscriptionService.createSubscription(
                    tenantId,
                    request.getPlanName(),
                    request.getContactTier(),
                    BillingCycle.valueOf(request.getBillingCycle().toUpperCase()),
                    request.getPaymentProvider(),
                    request.getCustomerEmail(),
                    request.getCustomerName()
            );

            SubscriptionDto.SubscriptionResponse response = mapSubscriptionToResponse(subscription);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Subscription created successfully"));

        } catch (Exception e) {
            log.error("Failed to create subscription", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to create subscription: " + e.getMessage()));
        }
    }

    /**
     * Change subscription plan
     */
    @PutMapping("/change")
    @Operation(summary = "Change subscription plan")
    public ResponseEntity<ApiResponse<SubscriptionDto.SubscriptionResponse>> changeSubscription(
            @Valid @RequestBody SubscriptionDto.ChangeSubscriptionRequest request) {

        Long tenantId = SecurityUtils.getTenantId();
        log.info("Changing subscription for tenant: {} to plan: {}", tenantId, request.getNewPlanName());

        try {
            OrganizationSubscription subscription = subscriptionService.changeSubscription(
                    tenantId,
                    request.getNewPlanName(),
                    request.getContactTier()
            );

            SubscriptionDto.SubscriptionResponse response = mapSubscriptionToResponse(subscription);
            return ResponseEntity.ok(ApiResponse.success(response, "Subscription changed successfully"));

        } catch (Exception e) {
            log.error("Failed to change subscription", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to change subscription: " + e.getMessage()));
        }
    }

    /**
     * Cancel subscription
     */
    @DeleteMapping
    @Operation(summary = "Cancel subscription")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(
            @Valid @RequestBody SubscriptionDto.CancelSubscriptionRequest request) {

        Long tenantId = SecurityUtils.getTenantId();
        log.info("Cancelling subscription for tenant: {}", tenantId);

        try {
            subscriptionService.cancelSubscription(
                    tenantId,
                    request.getReason(),
                    request.getImmediately()
            );

            return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully"));

        } catch (Exception e) {
            log.error("Failed to cancel subscription", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Failed to cancel subscription: " + e.getMessage()));
        }
    }

    /**
     * Get current usage statistics
     */
    @GetMapping("/usage")
    @Operation(summary = "Get current usage statistics")
    public ResponseEntity<ApiResponse<SubscriptionDto.UsageResponse>> getUsage() {
        Long tenantId = SecurityUtils.getTenantId();
        log.info("Getting usage statistics for tenant: {}", tenantId);

        UsageTracking usage = usageTrackingService.getCurrentUsage(tenantId);

        SubscriptionDto.UsageResponse response = SubscriptionDto.UsageResponse.builder()
                .contactCount(usage.getContactCount())
                .emailSentCount(usage.getEmailSentCount())
                .campaignCount(usage.getCampaignCount())
                .automationCount(usage.getAutomationCount())
                .userCount(usage.getUserCount())
                .trackingPeriod(usage.getTrackingPeriod())
                .contactUsagePercentage(subscriptionLimitService.getContactUsagePercentage(tenantId))
                .emailUsagePercentage(subscriptionLimitService.getEmailUsagePercentage(tenantId))
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get payment history
     */
    @GetMapping("/payments")
    @Operation(summary = "Get payment history")
    public ResponseEntity<ApiResponse<List<SubscriptionDto.PaymentResponse>>> getPayments() {
        Long tenantId = SecurityUtils.getTenantId();
        log.info("Getting payment history for tenant: {}", tenantId);

        List<Payment> payments = subscriptionService.getPaymentHistory(tenantId);

        List<SubscriptionDto.PaymentResponse> response = payments.stream()
                .map(this::mapPaymentToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get invoice history
     */
    @GetMapping("/invoices")
    @Operation(summary = "Get invoice history")
    public ResponseEntity<ApiResponse<List<SubscriptionDto.InvoiceResponse>>> getInvoices() {
        Long tenantId = SecurityUtils.getTenantId();
        log.info("Getting invoice history for tenant: {}", tenantId);

        List<Invoice> invoices = subscriptionService.getInvoices(tenantId);

        List<SubscriptionDto.InvoiceResponse> response = invoices.stream()
                .map(this::mapInvoiceToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Mapping methods

    private SubscriptionDto.SubscriptionPlanResponse mapPlanToResponse(SubscriptionPlan plan) {
        return SubscriptionDto.SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .displayName(plan.getDisplayName())
                .description(plan.getDescription())
                .contactLimit(plan.getContactLimit())
                .emailLimitPerMonth(plan.getEmailLimitPerMonth())
                .userLimit(plan.getUserLimit())
                .basePricePln(plan.getBasePricePln())
                .pricePer1000ContactsPln(plan.getPricePer1000ContactsPln())
                .isActive(plan.getIsActive())
                .build();
    }

    private SubscriptionDto.SubscriptionResponse mapSubscriptionToResponse(OrganizationSubscription subscription) {
        return SubscriptionDto.SubscriptionResponse.builder()
                .id(subscription.getId())
                .planName(subscription.getSubscriptionPlan().getName())
                .planDisplayName(subscription.getSubscriptionPlan().getDisplayName())
                .contactTier(subscription.getContactTier())
                .pricePln(subscription.getPricePln())
                .status(subscription.getStatus().name())
                .billingCycle(subscription.getBillingCycle().name())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .nextBillingDate(subscription.getNextBillingDate())
                .contactLimit(subscription.getEffectiveContactLimit())
                .emailLimit(subscription.getEffectiveEmailLimit())
                .userLimit(subscription.getEffectiveUserLimit())
                .externalSubscriptionId(subscription.getExternalSubscriptionId())
                .build();
    }

    private SubscriptionDto.PaymentResponse mapPaymentToResponse(Payment payment) {
        return SubscriptionDto.PaymentResponse.builder()
                .id(payment.getId())
                .amountPln(payment.getAmountPln())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod())
                .paidAt(payment.getPaidAt())
                .description(payment.getDescription())
                .build();
    }

    private SubscriptionDto.InvoiceResponse mapInvoiceToResponse(Invoice invoice) {
        return SubscriptionDto.InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .totalAmountPln(invoice.getTotalAmountPln())
                .currency(invoice.getCurrency())
                .status(invoice.getStatus().name())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .paidDate(invoice.getPaidDate())
                .pdfUrl(invoice.getPdfUrl())
                .fakturowniaUrl(invoice.getFakturowniaUrl())
                .build();
    }
}
