package com.mailist.mailist.apikey.infrastructure.repository;

import com.mailist.mailist.apikey.application.port.out.ApiKeyRepository;
import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import com.mailist.mailist.apikey.domain.valueobject.ApiKeyStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of ApiKeyRepository using JPA.
 */
@Component
@RequiredArgsConstructor
public class ApiKeyRepositoryImpl implements ApiKeyRepository {

    private final ApiKeyJpaRepository jpaRepository;

    @Override
    public ApiKey save(ApiKey apiKey) {
        return jpaRepository.save(apiKey);
    }

    @Override
    public Optional<ApiKey> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ApiKey> findByKeyHash(String keyHash) {
        return jpaRepository.findByKeyHash(keyHash);
    }

    @Override
    public List<ApiKey> findAllByOrganizationId(String organizationId) {
        return jpaRepository.findAllByOrganizationId(organizationId);
    }

    @Override
    public List<ApiKey> findByOrganizationIdAndStatus(String organizationId, ApiKeyStatus status) {
        return jpaRepository.findByOrganizationIdAndStatus(organizationId, status);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByOrganizationIdAndName(String organizationId, String name) {
        return jpaRepository.existsByOrganizationIdAndName(organizationId, name);
    }

    @Override
    public long countByOrganizationId(String organizationId) {
        return jpaRepository.countByOrganizationId(organizationId);
    }
}
