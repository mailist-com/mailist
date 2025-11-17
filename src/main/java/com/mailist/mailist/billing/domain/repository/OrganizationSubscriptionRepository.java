package com.mailist.mailist.billing.domain.repository;

import com.mailist.mailist.billing.domain.aggregate.OrganizationSubscription;
import com.mailist.mailist.billing.domain.valueobject.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for OrganizationSubscription aggregate
 */
@Repository
public interface OrganizationSubscriptionRepository extends JpaRepository<OrganizationSubscription, Long> {

    /**
     * Find active subscription for tenant
     */
    @Query("SELECT os FROM OrganizationSubscription os WHERE os.tenantId = :tenantId " +
           "AND os.status = 'ACTIVE' AND (os.endDate IS NULL OR os.endDate > :now) " +
           "ORDER BY os.createdAt DESC")
    Optional<OrganizationSubscription> findActiveByTenantId(
        @Param("tenantId") Long tenantId,
        @Param("now") LocalDateTime now
    );

    /**
     * Find all subscriptions for tenant
     */
    @Query("SELECT os FROM OrganizationSubscription os WHERE os.tenantId = :tenantId " +
           "ORDER BY os.createdAt DESC")
    List<OrganizationSubscription> findAllByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Find by external subscription ID
     */
    Optional<OrganizationSubscription> findByExternalSubscriptionId(String externalSubscriptionId);

    /**
     * Find subscriptions that need billing
     */
    @Query("SELECT os FROM OrganizationSubscription os WHERE os.status = 'ACTIVE' " +
           "AND os.nextBillingDate <= :date")
    List<OrganizationSubscription> findSubscriptionsDueForBilling(@Param("date") LocalDateTime date);

    /**
     * Find subscriptions by status
     */
    List<OrganizationSubscription> findByStatus(SubscriptionStatus status);

    /**
     * Count active subscriptions for tenant
     */
    @Query("SELECT COUNT(os) FROM OrganizationSubscription os WHERE os.tenantId = :tenantId " +
           "AND os.status = 'ACTIVE'")
    long countActiveByTenantId(@Param("tenantId") Long tenantId);
}
