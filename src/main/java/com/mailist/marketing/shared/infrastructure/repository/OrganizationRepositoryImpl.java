package com.mailist.marketing.shared.infrastructure.repository;

import com.mailist.marketing.shared.application.port.out.OrganizationRepository;
import com.mailist.marketing.shared.domain.aggregate.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of OrganizationRepository using JPA
 */
@Repository
@RequiredArgsConstructor
public class OrganizationRepositoryImpl implements OrganizationRepository {
    
    private final OrganizationJpaRepository jpaRepository;
    
    @Override
    public Organization save(Organization organization) {
        return jpaRepository.save(organization);
    }
    
    @Override
    public Optional<Organization> findById(Long id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public Optional<Organization> findBySubdomain(String subdomain) {
        return jpaRepository.findBySubdomain(subdomain);
    }
    
    @Override
    public Optional<Organization> findByOwnerEmail(String ownerEmail) {
        return jpaRepository.findByOwnerEmail(ownerEmail);
    }
    
    @Override
    public boolean existsBySubdomain(String subdomain) {
        return jpaRepository.existsBySubdomain(subdomain);
    }
    
    @Override
    public boolean existsByOwnerEmail(String ownerEmail) {
        return jpaRepository.existsByOwnerEmail(ownerEmail);
    }
    
    @Override
    public Page<Organization> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }
    
    @Override
    public List<Organization> findByStatus(Organization.Status status) {
        return jpaRepository.findByStatus(status);
    }
    
    @Override
    public List<Organization> findByPlan(Organization.Plan plan) {
        return jpaRepository.findByPlan(plan);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public long count() {
        return jpaRepository.count();
    }
}