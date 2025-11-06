package com.mailist.mailist.apikey.application.usecase;

import com.mailist.mailist.apikey.application.port.out.ApiKeyActivityRepository;
import com.mailist.mailist.apikey.application.port.out.ApiKeyRepository;
import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for revoking an API key.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RevokeApiKeyUseCase {

    private final ApiKeyRepository repository;
    private final ApiKeyActivityRepository activityRepository;

    @Transactional
    public void execute(Long apiKeyId) {
        long organizationId = TenantContext.getOrganizationId();

        ApiKey apiKey = repository.findById(apiKeyId)
                .filter(key -> key.getTenantId().equals(organizationId))
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + apiKeyId));

        apiKey.revoke();
        repository.save(apiKey);

        log.info("Revoked API key: {} for organization: {}", apiKeyId, organizationId);
    }

    @Transactional
    public void delete(Long apiKeyId) {
        long organizationId = TenantContext.getOrganizationId();

        ApiKey apiKey = repository.findById(apiKeyId)
                .filter(key -> key.getTenantId().equals(organizationId))
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + apiKeyId));

        // Delete associated activities
        activityRepository.deleteByApiKeyId(apiKeyId);

        // Delete the key
        repository.deleteById(apiKeyId);

        log.info("Deleted API key: {} for organization: {}", apiKeyId, organizationId);
    }
}
