package com.mailist.mailist.apikey.application.usecase;

import com.mailist.mailist.apikey.application.port.out.ApiKeyRepository;
import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import com.mailist.mailist.apikey.domain.valueobject.ApiKeyStatus;
import com.mailist.mailist.apikey.infrastructure.security.ApiKeyGenerator;
import com.mailist.mailist.shared.infrastructure.security.SecurityUtils;
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Use case for creating a new API key.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateApiKeyUseCase {

    private final ApiKeyRepository repository;
    private final ApiKeyGenerator keyGenerator;

    @Transactional
    public CreatedApiKey execute(CreateApiKeyCommand command) {
        String organizationId = TenantContext.getCurrentTenant();
        String userId = SecurityUtils.getCurrentUserId();

        // Check if name already exists
        if (repository.existsByOrganizationIdAndName(organizationId, command.name())) {
            throw new IllegalArgumentException("API key with name '" + command.name() + "' already exists");
        }

        // Generate key
        ApiKeyGenerator.GeneratedKey generatedKey = keyGenerator.generateKey(false);

        // Create API key entity
        ApiKey apiKey = ApiKey.builder()
                .name(command.name())
                .description(command.description())
                .keyHash(generatedKey.keyHash())
                .keyPrefix(generatedKey.prefix())
                .lastFourChars(generatedKey.lastFour())
                .status(ApiKeyStatus.ACTIVE)
                .permissions(command.permissions())
                .createdBy(userId)
                .expiresAt(command.expiresAt())
                .build();

        apiKey.setOrganizationId(organizationId);

        // Save
        ApiKey saved = repository.save(apiKey);
        log.info("Created API key: {} for organization: {}", saved.getId(), organizationId);

        // Return with plain key (this is the only time it's visible!)
        return new CreatedApiKey(saved, generatedKey.plainKey());
    }

    public record CreateApiKeyCommand(
            String name,
            String description,
            Set<String> permissions,
            LocalDateTime expiresAt
    ) {
    }

    public record CreatedApiKey(
            ApiKey apiKey,
            String plainKey
    ) {
    }
}
