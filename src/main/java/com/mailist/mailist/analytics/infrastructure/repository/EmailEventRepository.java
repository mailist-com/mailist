package com.mailist.mailist.analytics.infrastructure.repository;

import com.mailist.mailist.analytics.domain.aggregate.EmailEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailEventRepository extends JpaRepository<EmailEvent, Long> {

    List<EmailEvent> findByCampaignId(String campaignId);

    List<EmailEvent> findByContactEmail(String contactEmail);

    @Query("SELECT COUNT(DISTINCT e.contactEmail) FROM EmailEvent e " +
           "WHERE e.eventType = 'SENT' " +
           "AND e.createdAt >= :startDate " +
           "AND e.tenantId = :tenantId")
    Long countSentEmailsByTenantSince(@Param("tenantId") Long tenantId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(DISTINCT e.messageId) FROM EmailEvent e " +
           "WHERE e.eventType = 'OPENED' " +
           "AND e.createdAt >= :startDate " +
           "AND e.tenantId = :tenantId")
    Long countOpenedEmailsByTenantSince(@Param("tenantId") Long tenantId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(DISTINCT e.messageId) FROM EmailEvent e " +
           "WHERE e.eventType = 'CLICKED' " +
           "AND e.createdAt >= :startDate " +
           "AND e.tenantId = :tenantId")
    Long countClickedEmailsByTenantSince(@Param("tenantId") Long tenantId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT e.campaignId, COUNT(DISTINCT e.messageId) FROM EmailEvent e " +
           "WHERE e.eventType = 'SENT' " +
           "AND e.tenantId = :tenantId " +
           "GROUP BY e.campaignId")
    List<Object[]> countSentByCampaign(@Param("tenantId") Long tenantId);

    @Query("SELECT e.campaignId, COUNT(DISTINCT e.messageId) FROM EmailEvent e " +
           "WHERE e.eventType = 'OPENED' " +
           "AND e.tenantId = :tenantId " +
           "GROUP BY e.campaignId")
    List<Object[]> countOpensByCampaign(@Param("tenantId") Long tenantId);

    @Query("SELECT e.campaignId, COUNT(DISTINCT e.messageId) FROM EmailEvent e " +
           "WHERE e.eventType = 'CLICKED' " +
           "AND e.tenantId = :tenantId " +
           "GROUP BY e.campaignId")
    List<Object[]> countClicksByCampaign(@Param("tenantId") Long tenantId);

    @Query("SELECT EXTRACT(MONTH FROM e.createdAt) as month, COUNT(DISTINCT e.messageId) " +
           "FROM EmailEvent e " +
           "WHERE e.eventType = 'SENT' " +
           "AND EXTRACT(YEAR FROM e.createdAt) = :year " +
           "AND e.tenantId = :tenantId " +
           "GROUP BY EXTRACT(MONTH FROM e.createdAt) " +
           "ORDER BY month")
    List<Object[]> countSentByMonth(@Param("tenantId") Long tenantId, @Param("year") int year);
}
