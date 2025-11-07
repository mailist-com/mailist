package com.mailist.mailist.apikey.application.usecase;

import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import com.mailist.mailist.apikey.infrastructure.repository.ApiKeyRepository;
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for toggling API key status (ACTIVE <-> REVOKED).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ToggleApiKeyStatusUseCase {

    private final ApiKeyRepository repository;

    @Transactional
    public ApiKey execute(Long apiKeyId) {
        long organizationId = TenantContext.getOrganizationId();

        ApiKey apiKey = repository.findById(apiKeyId)
                .filter(key -> key.getTenantId().equals(organizationId))
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + apiKeyId));

        apiKey.toggleStatus();
        ApiKey updated = repository.save(apiKey);

        log.info("Toggled API key status: {} to {} for organization: {}",
                apiKeyId, apiKey.getStatus(), organizationId);

        return updated;
    }
}
