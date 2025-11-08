package com.mailist.mailist.contact.infrastructure.repository;

import com.mailist.mailist.contact.domain.aggregate.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    Optional<Contact> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Contact> findByEmail(String email);

    Optional<Contact> findByEmailAndTenantId(String email, Long tenantId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndTenantId(String email, Long tenantId);

    List<Contact> findByTenantId(Long tenantId);

    org.springframework.data.domain.Page<Contact> findByTenantId(Long tenantId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT c FROM Contact c JOIN c.tags t WHERE t.name = :tagName AND c.tenantId = :tenantId")
    List<Contact> findByTagsNameAndTenantId(@Param("tagName") String tagName, @Param("tenantId") Long tenantId);

    List<Contact> findByLeadScoreBetweenAndTenantId(int minScore, int maxScore, Long tenantId);

    long countByTenantId(Long tenantId);

    @Query("SELECT c FROM Contact c WHERE c.leadScore >= :minScore AND c.tenantId = :tenantId")
    List<Contact> findByLeadScoreGreaterThanEqualAndTenantId(@Param("minScore") int minScore, @Param("tenantId") Long tenantId);

    @Query("SELECT c FROM Contact c WHERE c.lastActivityAt >= :date AND c.tenantId = :tenantId")
    List<Contact> findByLastActivityAfterAndTenantId(@Param("date") java.time.LocalDateTime date, @Param("tenantId") Long tenantId);

    @Query("SELECT c FROM Contact c JOIN c.tags t WHERE t.name = :tagName")
    List<Contact> findByTagsName(@Param("tagName") String tagName);

    List<Contact> findByLeadScoreBetween(int minScore, int maxScore);

    @Query("SELECT c FROM Contact c WHERE c.leadScore >= :minScore")
    List<Contact> findByLeadScoreGreaterThanEqual(@Param("minScore") int minScore);

    @Query("SELECT c FROM Contact c WHERE c.lastActivityAt >= :date")
    List<Contact> findByLastActivityAfter(@Param("date") java.time.LocalDateTime date);

    @Query("SELECT COUNT(c) FROM Contact c WHERE c.createdAt >= :date AND c.tenantId = :tenantId")
    Long countByCreatedAtAfterAndTenantId(@Param("date") java.time.LocalDateTime date, @Param("tenantId") Long tenantId);

    Long countByCreatedAtAfter(java.time.LocalDateTime date);

    Long countByCreatedAtBefore(java.time.LocalDateTime date);

    @Query("SELECT EXTRACT(MONTH FROM c.createdAt) as month, COUNT(c) " +
           "FROM Contact c " +
           "WHERE EXTRACT(YEAR FROM c.createdAt) = :year " +
           "AND c.tenantId = :tenantId " +
           "GROUP BY EXTRACT(MONTH FROM c.createdAt) " +
           "ORDER BY month")
    List<Object[]> countByMonth(@Param("tenantId") Long tenantId, @Param("year") int year);
}