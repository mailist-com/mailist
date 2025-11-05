package com.mailist.mailist.shared.infrastructure.repository;

import com.mailist.mailist.shared.domain.aggregate.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Organization entity
 */
public interface OrganizationJpaRepository extends JpaRepository<Organization, Long> {
    
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
     * Find organizations by status
     */
    List<Organization> findByStatus(Organization.Status status);
    
    /**
     * Find organizations by plan
     */
    List<Organization> findByPlan(Organization.Plan plan);
    
    /**
     * Find active organizations
     */
    @Query("SELECT o FROM Organization o WHERE o.status = 'ACTIVE'")
    List<Organization> findAllActive();
    
    /**
     * Find organizations with contact count
     */
    @Query("SELECT o FROM Organization o WHERE o.id = :organizationId")
    Optional<Organization> findByIdWithDetails(@Param("organizationId") Long organizationId);
}