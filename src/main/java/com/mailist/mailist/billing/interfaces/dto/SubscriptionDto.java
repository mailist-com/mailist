package com.mailist.mailist.billing.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTOs for Subscription API
 */
public class SubscriptionDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSubscriptionRequest {
        @NotBlank(message = "Plan name is required")
        private String planName;

        @NotNull(message = "Contact tier is required")
        @Min(value = 1, message = "Contact tier must be at least 1")
        private Integer contactTier;

        @NotBlank(message = "Billing cycle is required")
        private String billingCycle; // MONTHLY, QUARTERLY, YEARLY

        @NotBlank(message = "Customer email is required")
        private String customerEmail;

        @NotBlank(message = "Customer name is required")
        private String customerName;

        private String paymentProvider = "stripe"; // Default to Stripe
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeSubscriptionRequest {
        @NotBlank(message = "New plan name is required")
        private String newPlanName;

        @NotNull(message = "Contact tier is required")
        @Min(value = 1, message = "Contact tier must be at least 1")
        private Integer contactTier;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelSubscriptionRequest {
        @NotBlank(message = "Cancellation reason is required")
        private String reason;

        private Boolean immediately = false;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionResponse {
        private Long id;
        private String planName;
        private String planDisplayName;
        private Integer contactTier;
        private BigDecimal pricePln;
        private String status;
        private String billingCycle;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private LocalDateTime nextBillingDate;
        private Integer contactLimit;
        private Integer emailLimit;
        private Integer userLimit;
        private String externalSubscriptionId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionPlanResponse {
        private Long id;
        private String name;
        private String displayName;
        private String description;
        private Integer contactLimit;
        private Integer emailLimitPerMonth;
        private Integer userLimit;
        private BigDecimal basePricePln;
        private BigDecimal pricePer1000ContactsPln;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageResponse {
        private Integer contactCount;
        private Integer emailSentCount;
        private Integer campaignCount;
        private Integer automationCount;
        private Integer userCount;
        private String trackingPeriod;
        private Integer contactUsagePercentage;
        private Integer emailUsagePercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResponse {
        private Long id;
        private BigDecimal amountPln;
        private String currency;
        private String status;
        private String paymentMethod;
        private LocalDateTime paidAt;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceResponse {
        private Long id;
        private String invoiceNumber;
        private BigDecimal totalAmountPln;
        private String currency;
        private String status;
        private java.time.LocalDate invoiceDate;
        private java.time.LocalDate dueDate;
        private java.time.LocalDate paidDate;
        private String pdfUrl;
        private String fakturowniaUrl;
    }
}
