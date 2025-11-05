package com.mailist.mailist.contact.infrastructure.repository;

import com.mailist.mailist.contact.application.port.out.ContactRepository;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactJpaRepository extends JpaRepository<Contact, Long>, ContactRepository {

    // Multi-tenant aware methods
    @Override
    Optional<Contact> findByIdAndTenantId(Long id, Long tenantId);

    @Override
    Optional<Contact> findByEmail(String email);

    @Override
    Optional<Contact> findByEmailAndTenantId(String email, Long tenantId);

    @Override
    boolean existsByEmail(String email);

    @Override
    boolean existsByEmailAndTenantId(String email, Long tenantId);

    // Tenant-scoped queries
    @Override
    List<Contact> findByTenantId(Long tenantId);

    @Override
    org.springframework.data.domain.Page<Contact> findByTenantId(Long tenantId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT c FROM Contact c JOIN c.tags t WHERE t.name = :tagName AND c.tenantId = :tenantId")
    @Override
    List<Contact> findByTagsNameAndTenantId(@Param("tagName") String tagName, @Param("tenantId") Long tenantId);

    @Override
    List<Contact> findByLeadScoreBetweenAndTenantId(int minScore, int maxScore, Long tenantId);

    @Override
    long countByTenantId(Long tenantId);

    @Query("SELECT c FROM Contact c WHERE c.leadScore >= :minScore AND c.tenantId = :tenantId")
    List<Contact> findByLeadScoreGreaterThanEqualAndTenantId(@Param("minScore") int minScore, @Param("tenantId") Long tenantId);

    @Query("SELECT c FROM Contact c WHERE c.lastActivityAt >= :date AND c.tenantId = :tenantId")
    List<Contact> findByLastActivityAfterAndTenantId(@Param("date") java.time.LocalDateTime date, @Param("tenantId") Long tenantId);

    // Legacy methods (global - for backward compatibility)
    @Query("SELECT c FROM Contact c JOIN c.tags t WHERE t.name = :tagName")
    @Override
    List<Contact> findByTagsName(@Param("tagName") String tagName);

    @Override
    List<Contact> findByLeadScoreBetween(int minScore, int maxScore);

    @Query("SELECT c FROM Contact c WHERE c.leadScore >= :minScore")
    List<Contact> findByLeadScoreGreaterThanEqual(@Param("minScore") int minScore);

    @Query("SELECT c FROM Contact c WHERE c.lastActivityAt >= :date")
    List<Contact> findByLastActivityAfter(@Param("date") java.time.LocalDateTime date);
}