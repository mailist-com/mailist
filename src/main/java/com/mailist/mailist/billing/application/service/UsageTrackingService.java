package com.mailist.mailist.billing.application.service;

import com.mailist.mailist.billing.domain.aggregate.UsageTracking;
import com.mailist.mailist.billing.domain.repository.UsageTrackingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for tracking resource usage per tenant
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UsageTrackingService {

    private final UsageTrackingRepository usageTrackingRepository;

    /**
     * Get or create current usage tracking for tenant
     */
    public UsageTracking getCurrentUsage(Long tenantId) {
        String currentPeriod = UsageTracking.getCurrentPeriod();
        return usageTrackingRepository.findByTenantIdAndTrackingPeriod(tenantId, currentPeriod)
            .orElseGet(() -> createNewUsageTracking(tenantId, currentPeriod));
    }

    /**
     * Increment contact count
     */
    public void incrementContactCount(Long tenantId, int count) {
        UsageTracking usage = getCurrentUsage(tenantId);
        usage.incrementContactCount(count);
        usageTrackingRepository.save(usage);
        log.debug("Incremented contact count for tenant {}: +{}", tenantId, count);
    }

    /**
     * Decrement contact count
     */
    public void decrementContactCount(Long tenantId, int count) {
        UsageTracking usage = getCurrentUsage(tenantId);
        usage.decrementContactCount(count);
        usageTrackingRepository.save(usage);
        log.debug("Decremented contact count for tenant {}: -{}", tenantId, count);
    }

    /**
     * Increment email sent count
     */
    public void incrementEmailSentCount(Long tenantId, int count) {
        UsageTracking usage = getCurrentUsage(tenantId);
        usage.incrementEmailSentCount(count);
        usageTrackingRepository.save(usage);
        log.debug("Incremented email sent count for tenant {}: +{}", tenantId, count);
    }

    /**
     * Increment campaign count
     */
    public void incrementCampaignCount(Long tenantId) {
        UsageTracking usage = getCurrentUsage(tenantId);
        usage.incrementCampaignCount();
        usageTrackingRepository.save(usage);
        log.debug("Incremented campaign count for tenant {}", tenantId);
    }

    /**
     * Increment automation count
     */
    public void incrementAutomationCount(Long tenantId) {
        UsageTracking usage = getCurrentUsage(tenantId);
        usage.incrementAutomationCount();
        usageTrackingRepository.save(usage);
        log.debug("Incremented automation count for tenant {}", tenantId);
    }

    /**
     * Decrement automation count
     */
    public void decrementAutomationCount(Long tenantId) {
        UsageTracking usage = getCurrentUsage(tenantId);
        usage.decrementAutomationCount();
        usageTrackingRepository.save(usage);
        log.debug("Decremented automation count for tenant {}", tenantId);
    }

    /**
     * Update user count
     */
    public void updateUserCount(Long tenantId, int count) {
        UsageTracking usage = getCurrentUsage(tenantId);
        usage.setUserCount(count);
        usageTrackingRepository.save(usage);
        log.debug("Updated user count for tenant {}: {}", tenantId, count);
    }

    /**
     * Get usage history for tenant
     */
    @Transactional(readOnly = true)
    public List<UsageTracking> getUsageHistory(Long tenantId) {
        return usageTrackingRepository.findAllByTenantId(tenantId);
    }

    /**
     * Reset monthly counters for new month
     */
    public void resetMonthlyCounters(Long tenantId) {
        String currentPeriod = UsageTracking.getCurrentPeriod();
        UsageTracking usage = createNewUsageTracking(tenantId, currentPeriod);
        usageTrackingRepository.save(usage);
        log.info("Reset monthly counters for tenant {}", tenantId);
    }

    /**
     * Create new usage tracking entry
     */
    private UsageTracking createNewUsageTracking(Long tenantId, String period) {
        return UsageTracking.builder()
            .tenantId(tenantId)
            .trackingPeriod(period)
            .contactCount(0)
            .emailSentCount(0)
            .campaignCount(0)
            .automationCount(0)
            .userCount(0)
            .build();
    }

    /**
     * Calculate usage percentage for resource
     */
    @Transactional(readOnly = true)
    public int getUsagePercentage(Long tenantId, String resource, int limit) {
        UsageTracking usage = getCurrentUsage(tenantId);
        return usage.calculateUsagePercentage(resource, limit);
    }
}
