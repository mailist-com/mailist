package com.mailist.mailist.contact.application.port.out;

import com.mailist.mailist.contact.domain.aggregate.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;

public interface ContactRepository {
    Contact save(Contact contact);
    Optional<Contact> findById(Long id);

    // Multi-tenant aware methods
    Optional<Contact> findByIdAndTenantId(Long id, Long tenantId);
    Optional<Contact> findByEmail(String email);
    Optional<Contact> findByEmailAndTenantId(String email, Long tenantId);
    boolean existsByEmail(String email);
    boolean existsByEmailAndTenantId(String email, Long tenantId);

    // Tenant-scoped queries
    List<Contact> findByTenantId(Long tenantId);
    Page<Contact> findByTenantId(Long tenantId, Pageable pageable);
    List<Contact> findByTagsNameAndTenantId(String tagName, Long tenantId);
    List<Contact> findByLeadScoreBetweenAndTenantId(int minScore, int maxScore, Long tenantId);
    long countByTenantId(Long tenantId);

    // Legacy methods (global - will be deprecated in favor of tenant-aware versions)
    Page<Contact> findAll(Pageable pageable);
    List<Contact> findByTagsName(String tagName);
    List<Contact> findByLeadScoreBetween(int minScore, int maxScore);
    void deleteById(Long id);
    long count();
    boolean existsById(Long id);
}