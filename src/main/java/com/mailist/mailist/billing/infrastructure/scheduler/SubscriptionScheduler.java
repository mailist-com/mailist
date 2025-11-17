package com.mailist.mailist.billing.infrastructure.scheduler;

import com.mailist.mailist.billing.application.service.UsageTrackingService;
import com.mailist.mailist.billing.domain.aggregate.SubscriptionNotification;
import com.mailist.mailist.billing.domain.repository.SubscriptionNotificationRepository;
import com.mailist.mailist.billing.domain.repository.UsageTrackingRepository;
import com.mailist.mailist.shared.domain.aggregate.Organization;
import com.mailist.mailist.shared.infrastructure.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled jobs for subscription management
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final UsageTrackingRepository usageTrackingRepository;
    private final SubscriptionNotificationRepository notificationRepository;
    private final OrganizationRepository organizationRepository;

    /**
     * Reset monthly counters on the 1st day of each month at 00:00
     * Cron: second, minute, hour, day of month, month, day of week
     */
    @Scheduled(cron = "0 0 0 1 * *")
    public void resetMonthlyCounters() {
        log.info("Starting monthly usage counter reset");

        try {
            List<Organization> organizations = organizationRepository.findAll();

            for (Organization org : organizations) {
                try {
                    String currentPeriod = com.mailist.mailist.billing.domain.aggregate.UsageTracking.getCurrentPeriod();

                    // Find current usage
                    var usageOpt = usageTrackingRepository.findByTenantIdAndTrackingPeriod(
                            org.getId(),
                            currentPeriod
                    );

                    if (usageOpt.isPresent()) {
                        var usage = usageOpt.get();

                        // Reset monthly counters but keep contact count
                        usage.resetMonthlyCounters();
                        usageTrackingRepository.save(usage);

                        log.info("Reset monthly counters for tenant: {}", org.getId());
                    }

                } catch (Exception e) {
                    log.error("Failed to reset counters for tenant: {}", org.getId(), e);
                }
            }

            log.info("Completed monthly usage counter reset");

        } catch (Exception e) {
            log.error("Error during monthly counter reset", e);
        }
    }

    /**
     * Send pending subscription notifications every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    public void sendPendingNotifications() {
        log.info("Checking for pending subscription notifications");

        try {
            List<SubscriptionNotification> notifications =
                    notificationRepository.findUnsentNotifications();

            log.info("Found {} pending notifications to send", notifications.size());

            for (SubscriptionNotification notification : notifications) {
                try {
                    // TODO: Send notification via email/in-app notification system
                    // For now, just mark as sent
                    notification.markSent();
                    notificationRepository.save(notification);

                    log.info("Sent notification {} to tenant {}",
                            notification.getNotificationType(),
                            notification.getTenantId());

                } catch (Exception e) {
                    log.error("Failed to send notification: {}", notification.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("Error sending pending notifications", e);
        }
    }

    /**
     * Check for expiring subscriptions every day at 09:00
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkExpiringSubscriptions() {
        log.info("Checking for expiring subscriptions");

        // TODO: Implement logic to:
        // - Find subscriptions expiring in 7 days
        // - Send renewal reminders
        // - Find subscriptions expiring in 1 day
        // - Send urgent renewal reminders

        log.info("Completed expiring subscription check");
    }
}
