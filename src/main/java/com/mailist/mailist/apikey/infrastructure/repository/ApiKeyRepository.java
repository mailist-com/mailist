package com.mailist.mailist.apikey.infrastructure.repository;

import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import com.mailist.mailist.apikey.domain.valueobject.ApiKeyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for ApiKey.
 */
@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByKeyHash(String keyHash);

    List<ApiKey> findAllByTenantId(long tenantId);

    List<ApiKey> findByTenantIdAndStatus(long tenantId, ApiKeyStatus status);

    boolean existsByTenantIdAndName(long tenantId, String name);

    long countByTenantId(long tenantId);
}
