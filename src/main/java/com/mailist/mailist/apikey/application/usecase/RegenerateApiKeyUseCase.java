package com.mailist.mailist.apikey.application.usecase;

import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import com.mailist.mailist.apikey.infrastructure.repository.ApiKeyRepository;
import com.mailist.mailist.apikey.infrastructure.security.ApiKeyGenerator;
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for regenerating an API key.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RegenerateApiKeyUseCase {

    private final ApiKeyRepository repository;
    private final ApiKeyGenerator keyGenerator;

    public record RegeneratedApiKey(
            ApiKey apiKey,
            String plainKey
    ) {}

    @Transactional
    public RegeneratedApiKey execute(Long apiKeyId) {
        long organizationId = TenantContext.getOrganizationId();

        ApiKey apiKey = repository.findById(apiKeyId)
                .filter(key -> key.getTenantId().equals(organizationId))
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + apiKeyId));

        if (apiKey.getStatus() != com.mailist.mailist.apikey.domain.valueobject.ApiKeyStatus.ACTIVE) {
            throw new IllegalStateException("Cannot regenerate a non-active API key");
        }

        // Generate new key
        boolean isTest = apiKey.getKeyPrefix().contains("test");
        ApiKeyGenerator.GeneratedKey generated = keyGenerator.generateKey(isTest);

        // Update the API key with new values
        apiKey.setKeyHash(generated.keyHash());
        apiKey.setKeyPrefix(generated.prefix());
        apiKey.setLastFourChars(generated.lastFour());
        apiKey.setTotalCalls(0L);
        apiKey.setLastUsedAt(null);
        apiKey.setLastUsedIpAddress(null);

        ApiKey updated = repository.save(apiKey);

        log.info("Regenerated API key: {} for organization: {}", apiKeyId, organizationId);

        return new RegeneratedApiKey(updated, generated.plainKey());
    }
}
