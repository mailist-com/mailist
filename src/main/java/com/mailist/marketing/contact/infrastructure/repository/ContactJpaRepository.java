package com.mailist.marketing.contact.infrastructure.repository;

import com.mailist.marketing.contact.domain.aggregate.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactJpaRepository extends JpaRepository<Contact, Long> {
    
    // Multi-tenant aware methods
    Optional<Contact> findByIdAndOrganizationId(Long id, Long organizationId);
    Optional<Contact> findByEmail(String email);
    Optional<Contact> findByEmailAndOrganizationId(String email, Long organizationId);
    boolean existsByEmail(String email);
    boolean existsByEmailAndOrganizationId(String email, Long organizationId);
    
    // Organization-scoped queries
    List<Contact> findByOrganizationId(Long organizationId);
    org.springframework.data.domain.Page<Contact> findByOrganizationId(Long organizationId, org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT c FROM Contact c JOIN c.tags t WHERE t.name = :tagName AND c.organization.id = :organizationId")
    List<Contact> findByTagsNameAndOrganizationId(@Param("tagName") String tagName, @Param("organizationId") Long organizationId);
    
    List<Contact> findByLeadScoreBetweenAndOrganizationId(int minScore, int maxScore, Long organizationId);
    long countByOrganizationId(Long organizationId);
    
    @Query("SELECT c FROM Contact c WHERE c.leadScore >= :minScore AND c.organization.id = :organizationId")
    List<Contact> findByLeadScoreGreaterThanEqualAndOrganizationId(@Param("minScore") int minScore, @Param("organizationId") Long organizationId);
    
    @Query("SELECT c FROM Contact c WHERE c.lastActivityAt >= :date AND c.organization.id = :organizationId")
    List<Contact> findByLastActivityAfterAndOrganizationId(@Param("date") java.time.LocalDateTime date, @Param("organizationId") Long organizationId);
    
    // Legacy methods (global - for backward compatibility)
    @Query("SELECT c FROM Contact c JOIN c.tags t WHERE t.name = :tagName")
    List<Contact> findByTagsName(@Param("tagName") String tagName);
    
    List<Contact> findByLeadScoreBetween(int minScore, int maxScore);
    
    @Query("SELECT c FROM Contact c WHERE c.leadScore >= :minScore")
    List<Contact> findByLeadScoreGreaterThanEqual(@Param("minScore") int minScore);
    
    @Query("SELECT c FROM Contact c WHERE c.lastActivityAt >= :date")
    List<Contact> findByLastActivityAfter(@Param("date") java.time.LocalDateTime date);
}