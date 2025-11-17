package com.mailist.mailist.billing.application.service;

import com.mailist.mailist.billing.domain.aggregate.OrganizationSubscription;
import com.mailist.mailist.billing.domain.aggregate.SubscriptionNotification;
import com.mailist.mailist.billing.domain.aggregate.SubscriptionNotification.NotificationType;
import com.mailist.mailist.billing.domain.aggregate.SubscriptionNotification.ResourceType;
import com.mailist.mailist.billing.domain.aggregate.UsageTracking;
import com.mailist.mailist.billing.domain.repository.OrganizationSubscriptionRepository;
import com.mailist.mailist.billing.domain.repository.SubscriptionNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for enforcing subscription limits
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionLimitService {

    private final OrganizationSubscriptionRepository subscriptionRepository;
    private final UsageTrackingService usageTrackingService;
    private final SubscriptionNotificationRepository notificationRepository;

    // Warning thresholds
    private static final int WARNING_THRESHOLD_80 = 80;
    private static final int WARNING_THRESHOLD_90 = 90;
    private static final int WARNING_THRESHOLD_95 = 95;

    /**
     * Check if tenant can add contacts
     */
    @Transactional(readOnly = true)
    public boolean canAddContacts(Long tenantId, int count) {
        OrganizationSubscription subscription = getActiveSubscription(tenantId);
        if (subscription == null) {
            log.warn("No active subscription found for tenant: {}", tenantId);
            return false;
        }

        UsageTracking usage = usageTrackingService.getCurrentUsage(tenantId);
        int currentCount = usage.getContactCount();

        return subscription.canAddContacts(currentCount, count);
    }

    /**
     * Check if tenant can send emails
     */
    @Transactional(readOnly = true)
    public boolean canSendEmails(Long tenantId, int count) {
        OrganizationSubscription subscription = getActiveSubscription(tenantId);
        if (subscription == null) {
            log.warn("No active subscription found for tenant: {}", tenantId);
            return false;
        }

        UsageTracking usage = usageTrackingService.getCurrentUsage(tenantId);
        int currentCount = usage.getEmailSentCount();

        return subscription.canSendEmails(currentCount, count);
    }

    /**
     * Check if tenant can add users
     */
    @Transactional(readOnly = true)
    public boolean canAddUsers(Long tenantId, int count) {
        OrganizationSubscription subscription = getActiveSubscription(tenantId);
        if (subscription == null) {
            log.warn("No active subscription found for tenant: {}", tenantId);
            return false;
        }

        UsageTracking usage = usageTrackingService.getCurrentUsage(tenantId);
        int currentCount = usage.getUserCount();

        return subscription.canAddUsers(currentCount, count);
    }

    /**
     * Check and send notifications for resource usage
     */
    public void checkAndNotifyLimits(Long tenantId) {
        OrganizationSubscription subscription = getActiveSubscription(tenantId);
        if (subscription == null) {
            return;
        }

        UsageTracking usage = usageTrackingService.getCurrentUsage(tenantId);

        // Check contact limit
        if (!subscription.getSubscriptionPlan().isUnlimitedContacts()) {
            checkResourceLimit(
                tenantId,
                ResourceType.CONTACTS,
                usage.getContactCount(),
                subscription.getEffectiveContactLimit()
            );
        }

        // Check email limit
        if (!subscription.getSubscriptionPlan().isUnlimitedEmails()) {
            checkResourceLimit(
                tenantId,
                ResourceType.EMAILS,
                usage.getEmailSentCount(),
                subscription.getEffectiveEmailLimit()
            );
        }

        // Check user limit
        if (!subscription.getSubscriptionPlan().isUnlimitedUsers()) {
            checkResourceLimit(
                tenantId,
                ResourceType.USERS,
                usage.getUserCount(),
                subscription.getEffectiveUserLimit()
            );
        }
    }

    /**
     * Check specific resource limit and create notifications
     */
    private void checkResourceLimit(Long tenantId, ResourceType resourceType,
                                    int currentUsage, int limit) {
        if (limit <= 0) {
            return; // Unlimited
        }

        int usagePercentage = (int) ((currentUsage * 100.0) / limit);

        // Check if limit exceeded
        if (usagePercentage >= 100) {
            createNotificationIfNotExists(
                tenantId,
                NotificationType.LIMIT_EXCEEDED,
                resourceType,
                100,
                currentUsage,
                limit
            );
        }
        // Check warning thresholds
        else if (usagePercentage >= WARNING_THRESHOLD_95) {
            createNotificationIfNotExists(
                tenantId,
                NotificationType.LIMIT_WARNING,
                resourceType,
                WARNING_THRESHOLD_95,
                currentUsage,
                limit
            );
        } else if (usagePercentage >= WARNING_THRESHOLD_90) {
            createNotificationIfNotExists(
                tenantId,
                NotificationType.LIMIT_WARNING,
                resourceType,
                WARNING_THRESHOLD_90,
                currentUsage,
                limit
            );
        } else if (usagePercentage >= WARNING_THRESHOLD_80) {
            createNotificationIfNotExists(
                tenantId,
                NotificationType.LIMIT_WARNING,
                resourceType,
                WARNING_THRESHOLD_80,
                currentUsage,
                limit
            );
        }
    }

    /**
     * Create notification if similar one doesn't exist recently
     */
    private void createNotificationIfNotExists(Long tenantId, NotificationType type,
                                               ResourceType resourceType, int threshold,
                                               int currentUsage, int limit) {
        // Check if similar notification was sent in last 24 hours
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        var existing = notificationRepository.findRecentSimilarNotification(
            tenantId, type, resourceType, threshold, since
        );

        if (existing.isEmpty()) {
            SubscriptionNotification notification = SubscriptionNotification.builder()
                .tenantId(tenantId)
                .notificationType(type)
                .resourceType(resourceType)
                .thresholdPercentage(threshold)
                .currentUsage(currentUsage)
                .limitValue(limit)
                .isSent(false)
                .build();

            notification.setMessage(notification.generateMessage());
            notificationRepository.save(notification);

            log.info("Created {} notification for tenant {}: {} at {}%",
                type, tenantId, resourceType, threshold);
        }
    }

    /**
     * Get active subscription for tenant
     */
    private OrganizationSubscription getActiveSubscription(Long tenantId) {
        return subscriptionRepository.findActiveByTenantId(tenantId, LocalDateTime.now())
            .orElse(null);
    }

    /**
     * Get current usage percentage
     */
    @Transactional(readOnly = true)
    public int getContactUsagePercentage(Long tenantId) {
        OrganizationSubscription subscription = getActiveSubscription(tenantId);
        if (subscription == null || subscription.getSubscriptionPlan().isUnlimitedContacts()) {
            return 0;
        }

        UsageTracking usage = usageTrackingService.getCurrentUsage(tenantId);
        int limit = subscription.getEffectiveContactLimit();
        return (int) ((usage.getContactCount() * 100.0) / limit);
    }

    @Transactional(readOnly = true)
    public int getEmailUsagePercentage(Long tenantId) {
        OrganizationSubscription subscription = getActiveSubscription(tenantId);
        if (subscription == null || subscription.getSubscriptionPlan().isUnlimitedEmails()) {
            return 0;
        }

        UsageTracking usage = usageTrackingService.getCurrentUsage(tenantId);
        int limit = subscription.getEffectiveEmailLimit();
        return (int) ((usage.getEmailSentCount() * 100.0) / limit);
    }
}
