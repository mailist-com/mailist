package com.mailist.mailist.apikey.application.usecase;

import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import com.mailist.mailist.apikey.infrastructure.repository.ApiKeyRepository;
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Use case for updating an API key.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateApiKeyUseCase {

    private final ApiKeyRepository repository;

    public record UpdateApiKeyCommand(
            String name,
            String description,
            Set<String> permissions
    ) {}

    @Transactional
    public ApiKey execute(Long apiKeyId, UpdateApiKeyCommand command) {
        long organizationId = TenantContext.getOrganizationId();

        ApiKey apiKey = repository.findById(apiKeyId)
                .filter(key -> key.getTenantId().equals(organizationId))
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + apiKeyId));

        // Update details
        apiKey.updateDetails(command.name(), command.description());

        // Update permissions if provided
        if (command.permissions() != null && !command.permissions().isEmpty()) {
            apiKey.updatePermissions(command.permissions());
        }

        ApiKey updated = repository.save(apiKey);

        log.info("Updated API key: {} for organization: {}", apiKeyId, organizationId);

        return updated;
    }
}
