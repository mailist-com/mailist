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
    public Optional<ApiKey> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ApiKey> findByKeyHash(String keyHash) {
        return jpaRepository.findByKeyHash(keyHash);
    }

    @Override
    public List<ApiKey> findAllByTenantId(long tenantId) {
        return jpaRepository.findAllByTenantId(tenantId);
    }

    @Override
    public List<ApiKey> findByTenantIdAndStatus(long tenantId, ApiKeyStatus status) {
        return jpaRepository.findByTenantIdAndStatus(tenantId, status);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByTenantIdAndName(long tenantId, String name) {
        return jpaRepository.existsByTenantIdAndName(tenantId, name);
    }

    @Override
    public long countByTenantId(long tenantId) {
        return jpaRepository.countByTenantId(tenantId);
    }
}
