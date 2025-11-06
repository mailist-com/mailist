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

    Optional<ApiKey> findById(String id);

    Optional<ApiKey> findByKeyHash(String keyHash);

    List<ApiKey> findAllByOrganizationId(String organizationId);

    List<ApiKey> findByOrganizationIdAndStatus(String organizationId, ApiKeyStatus status);

    void deleteById(String id);

    boolean existsByOrganizationIdAndName(String organizationId, String name);

    long countByOrganizationId(String organizationId);
}
