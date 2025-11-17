package com.mailist.mailist.billing.domain.repository;

import com.mailist.mailist.billing.domain.aggregate.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SubscriptionPlan aggregate
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    /**
     * Find plan by name
     */
    Optional<SubscriptionPlan> findByName(String name);

    /**
     * Find all active plans
     */
    @Query("SELECT sp FROM SubscriptionPlan sp WHERE sp.isActive = true ORDER BY sp.basePricePln ASC")
    List<SubscriptionPlan> findAllActivePlans();

    /**
     * Check if plan exists by name
     */
    boolean existsByName(String name);
}
