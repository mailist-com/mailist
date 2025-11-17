package com.mailist.mailist.billing.domain.repository;

import com.mailist.mailist.billing.domain.aggregate.UsageTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UsageTracking aggregate
 */
@Repository
public interface UsageTrackingRepository extends JpaRepository<UsageTracking, Long> {

    /**
     * Find usage tracking for tenant and period
     */
    Optional<UsageTracking> findByTenantIdAndTrackingPeriod(Long tenantId, String trackingPeriod);

    /**
     * Find current usage for tenant
     */
    @Query("SELECT ut FROM UsageTracking ut WHERE ut.tenantId = :tenantId " +
           "AND ut.trackingPeriod = :period")
    Optional<UsageTracking> findCurrentUsage(
        @Param("tenantId") Long tenantId,
        @Param("period") String period
    );

    /**
     * Find all usage records for tenant
     */
    @Query("SELECT ut FROM UsageTracking ut WHERE ut.tenantId = :tenantId " +
           "ORDER BY ut.trackingPeriod DESC")
    List<UsageTracking> findAllByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Find usage records for specific period across all tenants
     */
    List<UsageTracking> findByTrackingPeriod(String trackingPeriod);

    /**
     * Get or create usage tracking for tenant and period
     */
    @Query("SELECT ut FROM UsageTracking ut WHERE ut.tenantId = :tenantId " +
           "AND ut.trackingPeriod = :period")
    Optional<UsageTracking> findOrCreate(
        @Param("tenantId") Long tenantId,
        @Param("period") String period
    );
}
