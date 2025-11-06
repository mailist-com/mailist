package com.mailist.mailist.apikey.application.usecase;

import com.mailist.mailist.apikey.application.port.out.ApiKeyRepository;
import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Use case for getting API key statistics.
 */
@Service
@RequiredArgsConstructor
public class GetApiKeyStatisticsUseCase {

    private final ApiKeyRepository repository;

    @Transactional(readOnly = true)
    public ApiKeyStatistics execute() {
        String organizationId = TenantContext.getCurrentTenant();
        List<ApiKey> allKeys = repository.findAllByOrganizationId(organizationId);

        long totalKeys = allKeys.size();
        long activeKeys = allKeys.stream().filter(ApiKey::isActive).count();
        long totalCalls = allKeys.stream().mapToLong(ApiKey::getTotalCalls).sum();

        // Get top endpoints (placeholder - would need actual implementation)
        Map<String, Long> topEndpoints = Map.of();

        return new ApiKeyStatistics(
                totalKeys,
                activeKeys,
                totalCalls,
                topEndpoints
        );
    }

    public record ApiKeyStatistics(
            long totalKeys,
            long activeKeys,
            long totalCalls,
            Map<String, Long> topEndpoints
    ) {
    }
}
