package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.application.usecase.dto.GlobalStatistics;
import com.mailist.mailist.contact.domain.model.ListStatistics;
import com.mailist.mailist.contact.infrastructure.repository.ContactListRepository;
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
final class GetGlobalListStatisticsUseCase {

    private final ContactListRepository contactListRepository;

    GlobalStatistics execute() {
        // Get tenantId from context (required for native SQL query)
        final Long tenantId = TenantContext.getOrganizationId();
        log.info("Calculating global list statistics for tenant: {}", tenantId);

        // Use dedicated SQL query to get statistics directly from database
        // Note: Native SQL queries don't support @TenantId automatic filtering
        final ListStatistics stats = contactListRepository.getGlobalStatistics(tenantId);

        // Calculate average engagement rate
        // TODO: This should be calculated via SQL query for better performance
        // For now, returning 0.0 as placeholder
        double averageEngagement = 0.0;

        final int totalLists = stats.getTotalLists() != null ? stats.getTotalLists().intValue() : 0;
        final int activeLists = stats.getActiveLists() != null ? stats.getActiveLists().intValue() : 0;
        final int totalSubscribers = stats.getTotalSubscribers() != null ? stats.getTotalSubscribers().intValue() : 0;

        log.info("Global statistics for tenant {}: {} total lists, {} active lists, {} total subscribers, {}% avg engagement",
                tenantId, totalLists, activeLists, totalSubscribers, averageEngagement);

        return GlobalStatistics.builder()
                .totalLists(totalLists)
                .activeLists(activeLists)
                .totalSubscribers(totalSubscribers)
                .averageEngagement(averageEngagement)
                .build();
    }
}
