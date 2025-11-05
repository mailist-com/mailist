package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.application.port.out.ContactListRepository;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GetGlobalListStatisticsUseCase {

    private final ContactListRepository contactListRepository;

    public GlobalStatistics execute() {
        log.info("Calculating global list statistics");

        List<ContactList> allLists = contactListRepository.findAll();

        int totalLists = allLists.size();
        int activeLists = (int) allLists.stream()
                .filter(list -> Boolean.TRUE.equals(list.getIsActive()))
                .count();

        int totalSubscribers = allLists.stream()
                .mapToInt(ContactList::getContactCount)
                .sum();

        // Calculate average engagement rate
        // Engagement = (active contacts / total contacts) * 100
        // Active = contacts with activity in last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        double averageEngagement = 0.0;
        if (!allLists.isEmpty()) {
            double totalEngagement = allLists.stream()
                    .filter(list -> list.getContactCount() > 0)
                    .mapToDouble(list -> calculateListEngagement(list, thirtyDaysAgo))
                    .average()
                    .orElse(0.0);

            averageEngagement = Math.round(totalEngagement * 100.0) / 100.0;
        }

        log.info("Global statistics: {} total lists, {} active lists, {} total subscribers, {}% avg engagement",
                totalLists, activeLists, totalSubscribers, averageEngagement);

        return GlobalStatistics.builder()
                .totalLists(totalLists)
                .activeLists(activeLists)
                .totalSubscribers(totalSubscribers)
                .averageEngagement(averageEngagement)
                .build();
    }

    private double calculateListEngagement(ContactList list, LocalDateTime thirtyDaysAgo) {
        int totalContacts = list.getContactCount();
        if (totalContacts == 0) {
            return 0.0;
        }

        long activeContacts = list.getContacts().stream()
                .filter(contact -> contact.getLastActivityAt() != null)
                .filter(contact -> contact.getLastActivityAt().isAfter(thirtyDaysAgo))
                .count();

        return (activeContacts * 100.0) / totalContacts;
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
