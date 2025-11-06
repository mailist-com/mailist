package com.mailist.mailist.apikey.application.usecase;

import com.mailist.mailist.apikey.application.port.out.ApiKeyActivityRepository;
import com.mailist.mailist.apikey.application.port.out.ApiKeyRepository;
import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import com.mailist.mailist.apikey.domain.aggregate.ApiKeyActivity;
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Use case for retrieving API key activities.
 */
@Service
@RequiredArgsConstructor
public class GetApiKeyActivitiesUseCase {

    private final ApiKeyActivityRepository activityRepository;
    private final ApiKeyRepository apiKeyRepository;

    @Transactional(readOnly = true)
    public Page<ApiKeyActivity> execute(String apiKeyId, Pageable pageable) {
        String organizationId = TenantContext.getCurrentTenant();

        // Verify API key belongs to organization
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .filter(key -> key.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + apiKeyId));

        return activityRepository.findByApiKeyId(apiKeyId, pageable);
    }

    @Transactional(readOnly = true)
    public long countRecentCalls(String apiKeyId, int hours) {
        String organizationId = TenantContext.getCurrentTenant();

        // Verify API key belongs to organization
        apiKeyRepository.findById(apiKeyId)
                .filter(key -> key.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new IllegalArgumentException("API key not found: " + apiKeyId));

        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return activityRepository.countByApiKeyIdAndTimestampAfter(apiKeyId, since);
    }
}
