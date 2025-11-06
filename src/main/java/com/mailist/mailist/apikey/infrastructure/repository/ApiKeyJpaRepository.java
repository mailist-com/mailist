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
public interface ApiKeyJpaRepository extends JpaRepository<ApiKey, String> {

    Optional<ApiKey> findByKeyHash(String keyHash);

    List<ApiKey> findAllByOrganizationId(String organizationId);

    List<ApiKey> findByOrganizationIdAndStatus(String organizationId, ApiKeyStatus status);

    boolean existsByOrganizationIdAndName(String organizationId, String name);

    long countByOrganizationId(String organizationId);
}
