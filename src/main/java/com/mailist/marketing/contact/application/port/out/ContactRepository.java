package com.mailist.marketing.contact.application.port.out;

import com.mailist.marketing.contact.domain.aggregate.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;

public interface ContactRepository {
    Contact save(Contact contact);
    Optional<Contact> findById(Long id);
    
    // Multi-tenant aware methods
    Optional<Contact> findByIdAndOrganizationId(Long id, Long organizationId);
    Optional<Contact> findByEmail(String email);
    Optional<Contact> findByEmailAndOrganizationId(String email, Long organizationId);
    boolean existsByEmail(String email);
    boolean existsByEmailAndOrganizationId(String email, Long organizationId);
    
    // Organization-scoped queries
    List<Contact> findByOrganizationId(Long organizationId);
    Page<Contact> findByOrganizationId(Long organizationId, Pageable pageable);
    List<Contact> findByTagsNameAndOrganizationId(String tagName, Long organizationId);
    List<Contact> findByLeadScoreBetweenAndOrganizationId(int minScore, int maxScore, Long organizationId);
    long countByOrganizationId(Long organizationId);
    
    // Legacy methods (global - will be deprecated in favor of tenant-aware versions)
    Page<Contact> findAll(Pageable pageable);
    List<Contact> findByTagsName(String tagName);
    List<Contact> findByLeadScoreBetween(int minScore, int maxScore);
    void deleteById(Long id);
    long count();
    boolean existsById(Long id);
}