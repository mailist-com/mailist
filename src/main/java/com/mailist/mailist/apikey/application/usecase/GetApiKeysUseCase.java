package com.mailist.mailist.apikey.application.usecase;

import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import com.mailist.mailist.apikey.infrastructure.repository.ApiKeyRepository;
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
        long organizationId = TenantContext.getOrganizationId();
        return repository.findAllByTenantId(organizationId);
    }

    @Transactional(readOnly = true)
    public ApiKey getById(Long id) {
        long organizationId = TenantContext.getOrganizationId();
        return repository.findById(id)
                .filter(key -> key.getTenantId().equals(organizationId))
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + id));
    }
}
