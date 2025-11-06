package com.mailist.mailist.apikey.application.usecase;

import com.mailist.mailist.apikey.application.port.out.ApiKeyRepository;
import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case for retrieving API keys.
 */
@Service
@RequiredArgsConstructor
public class GetApiKeysUseCase {

    private final ApiKeyRepository repository;

    @Transactional(readOnly = true)
    public List<ApiKey> execute() {
        String organizationId = TenantContext.getCurrentTenant();
        return repository.findAllByOrganizationId(organizationId);
    }

    @Transactional(readOnly = true)
    public ApiKey getById(String id) {
        String organizationId = TenantContext.getCurrentTenant();
        return repository.findById(id)
                .filter(key -> key.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + id));
    }
}
