package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.application.port.out.ContactListRepository;
import com.mailist.mailist.contact.domain.model.ListStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GetGlobalListStatisticsUseCase {

    private final ContactListRepository contactListRepository;

    public GlobalStatistics execute() {
        log.info("Calculating global list statistics");

        // Use dedicated SQL query to get statistics directly from database
        ListStatistics stats = contactListRepository.getGlobalStatistics();

        // Calculate average engagement rate
        // TODO: This should be calculated via SQL query for better performance
        // For now, returning 0.0 as placeholder
        double averageEngagement = 0.0;

        int totalLists = stats.getTotalLists() != null ? stats.getTotalLists().intValue() : 0;
        int activeLists = stats.getActiveLists() != null ? stats.getActiveLists().intValue() : 0;
        int totalSubscribers = stats.getTotalSubscribers() != null ? stats.getTotalSubscribers().intValue() : 0;

        log.info("Global statistics: {} total lists, {} active lists, {} total subscribers, {}% avg engagement",
                totalLists, activeLists, totalSubscribers, averageEngagement);

        return GlobalStatistics.builder()
                .totalLists(totalLists)
                .activeLists(activeLists)
                .totalSubscribers(totalSubscribers)
                .averageEngagement(averageEngagement)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class GlobalStatistics {
        private Integer totalLists;
        private Integer activeLists;
        private Integer totalSubscribers;
        private Double averageEngagement;
    }
}
