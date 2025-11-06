package com.mailist.mailist.apikey.application.port.out;

import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import com.mailist.mailist.apikey.domain.valueobject.ApiKeyStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for ApiKey.
 */
public interface ApiKeyRepository {

    ApiKey save(ApiKey apiKey);

    Optional<ApiKey> findById(Long id);

    Optional<ApiKey> findByKeyHash(String keyHash);

    List<ApiKey> findAllByTenantId(long tenantId);

    List<ApiKey> findByTenantIdAndStatus(long tenantId, ApiKeyStatus status);

    void deleteById(Long id);

    boolean existsByTenantIdAndName(long tenantId, String name);

    long countByTenantId(long tenantId);
}
