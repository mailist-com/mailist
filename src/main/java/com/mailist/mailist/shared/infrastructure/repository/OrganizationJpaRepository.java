package com.mailist.mailist.shared.infrastructure.repository;

import com.mailist.mailist.shared.application.port.out.OrganizationRepository;
import com.mailist.mailist.shared.domain.aggregate.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Organization entity
 */
@Repository
public interface OrganizationJpaRepository extends JpaRepository<Organization, Long>, OrganizationRepository {

    /**
     * Find organization by subdomain
     */
    @Override
    Optional<Organization> findBySubdomain(String subdomain);

    /**
     * Find organization by owner email
     */
    @Override
    Optional<Organization> findByOwnerEmail(String ownerEmail);

    /**
     * Check if subdomain exists
     */
    @Override
    boolean existsBySubdomain(String subdomain);

    /**
     * Check if owner email exists
     */
    @Override
    boolean existsByOwnerEmail(String ownerEmail);

    /**
     * Find organizations by status
     */
    @Override
    List<Organization> findByStatus(Organization.Status status);

    /**
     * Find organizations by plan
     */
    @Override
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