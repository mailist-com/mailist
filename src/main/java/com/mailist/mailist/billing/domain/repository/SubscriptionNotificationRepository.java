package com.mailist.mailist.billing.domain.repository;

import com.mailist.mailist.billing.domain.aggregate.SubscriptionNotification;
import com.mailist.mailist.billing.domain.aggregate.SubscriptionNotification.NotificationType;
import com.mailist.mailist.billing.domain.aggregate.SubscriptionNotification.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SubscriptionNotification aggregate
 */
@Repository
public interface SubscriptionNotificationRepository extends JpaRepository<SubscriptionNotification, Long> {

    /**
     * Find unsent notifications
     */
    @Query("SELECT sn FROM SubscriptionNotification sn WHERE sn.isSent = false " +
           "ORDER BY sn.createdAt ASC")
    List<SubscriptionNotification> findUnsentNotifications();

    /**
     * Find notifications by tenant
     */
    @Query("SELECT sn FROM SubscriptionNotification sn WHERE sn.tenantId = :tenantId " +
           "ORDER BY sn.createdAt DESC")
    List<SubscriptionNotification> findByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Find recent notification for same threshold to avoid duplicates
     */
    @Query("SELECT sn FROM SubscriptionNotification sn WHERE sn.tenantId = :tenantId " +
           "AND sn.notificationType = :type AND sn.resourceType = :resourceType " +
           "AND sn.thresholdPercentage = :threshold " +
           "AND sn.createdAt > :since")
    Optional<SubscriptionNotification> findRecentSimilarNotification(
        @Param("tenantId") Long tenantId,
        @Param("type") NotificationType type,
        @Param("resourceType") ResourceType resourceType,
        @Param("threshold") Integer threshold,
        @Param("since") LocalDateTime since
    );

    /**
     * Find notifications by type
     */
    List<SubscriptionNotification> findByNotificationType(NotificationType notificationType);

    /**
     * Count unsent notifications for tenant
     */
    @Query("SELECT COUNT(sn) FROM SubscriptionNotification sn WHERE sn.tenantId = :tenantId " +
           "AND sn.isSent = false")
    long countUnsentByTenantId(@Param("tenantId") Long tenantId);
}
