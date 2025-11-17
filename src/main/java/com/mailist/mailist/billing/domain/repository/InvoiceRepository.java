package com.mailist.mailist.billing.domain.repository;

import com.mailist.mailist.billing.domain.aggregate.Invoice;
import com.mailist.mailist.billing.domain.valueobject.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Invoice aggregate
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /**
     * Find invoices by tenant
     */
    Page<Invoice> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    /**
     * Find by invoice number
     */
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    /**
     * Find by external invoice ID
     */
    Optional<Invoice> findByExternalInvoiceId(String externalInvoiceId);

    /**
     * Find invoices by status
     */
    List<Invoice> findByStatus(InvoiceStatus status);

    /**
     * Find overdue invoices
     */
    @Query("SELECT i FROM Invoice i WHERE i.status IN ('OPEN', 'SENT') " +
           "AND i.dueDate < :currentDate")
    List<Invoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);

    /**
     * Find paid invoices for tenant
     */
    @Query("SELECT i FROM Invoice i WHERE i.tenantId = :tenantId " +
           "AND i.status = 'PAID' ORDER BY i.paidDate DESC")
    List<Invoice> findPaidInvoicesByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Count invoices by tenant
     */
    long countByTenantId(Long tenantId);
}
