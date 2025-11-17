package com.mailist.mailist.billing.domain.repository;

import com.mailist.mailist.billing.domain.aggregate.Payment;
import com.mailist.mailist.billing.domain.valueobject.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Payment aggregate
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payments by tenant
     */
    Page<Payment> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    /**
     * Find by external payment ID
     */
    Optional<Payment> findByExternalPaymentId(String externalPaymentId);

    /**
     * Find payments by status
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Find payments by tenant and status
     */
    List<Payment> findByTenantIdAndStatus(Long tenantId, PaymentStatus status);

    /**
     * Find successful payments for tenant
     */
    @Query("SELECT p FROM Payment p WHERE p.tenantId = :tenantId " +
           "AND p.status = 'SUCCEEDED' ORDER BY p.paidAt DESC")
    List<Payment> findSuccessfulPaymentsByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Count payments by tenant
     */
    long countByTenantId(Long tenantId);
}
