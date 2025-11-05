package com.mailist.mailist.shared.application.port.out;

import com.mailist.mailist.shared.domain.aggregate.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Organization aggregate
 */
public interface OrganizationRepository {
    
    /**
     * Save an organization
     */
    Organization save(Organization organization);
    
    /**
     * Find organization by ID
     */
    Optional<Organization> findById(Long id);
    
    /**
     * Find organization by subdomain
     */
    Optional<Organization> findBySubdomain(String subdomain);
    
    /**
     * Find organization by owner email
     */
    Optional<Organization> findByOwnerEmail(String ownerEmail);
    
    /**
     * Check if subdomain exists
     */
    boolean existsBySubdomain(String subdomain);
    
    /**
     * Check if owner email exists
     */
    boolean existsByOwnerEmail(String ownerEmail);
    
    /**
     * Find all organizations with pagination
     */
    Page<Organization> findAll(Pageable pageable);
    
    /**
     * Find organizations by status
     */
    List<Organization> findByStatus(Organization.Status status);
    
    /**
     * Find organizations by plan
     */
    List<Organization> findByPlan(Organization.Plan plan);
    
    /**
     * Delete organization by ID
     */
    void deleteById(Long id);
    
    /**
     * Count total organizations
     */
    long count();
}