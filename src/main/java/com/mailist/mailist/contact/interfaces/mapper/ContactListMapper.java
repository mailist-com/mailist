package com.mailist.mailist.contact.interfaces.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailist.mailist.contact.application.usecase.command.CreateContactListCommand;
import com.mailist.mailist.contact.application.usecase.command.UpdateContactListCommand;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.interfaces.dto.ContactListDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContactListMapper {

    private final ObjectMapper objectMapper;

    /**
     * Maps ContactList entity to Response DTO
     */
    public ContactListDto.Response toResponse(ContactList list) {
        List<ContactListDto.SmartListCondition> conditions = null;

        // Parse segment rule if it's a smart list
        if (Boolean.TRUE.equals(list.getIsDynamic()) && list.getSegmentRule() != null) {
            try {
                conditions = objectMapper.readValue(
                    list.getSegmentRule(),
                    new TypeReference<List<ContactListDto.SmartListCondition>>() {}
                );
            } catch (JsonProcessingException e) {
                log.error("Failed to parse segment rule for list {}: {}", list.getId(), e.getMessage());
                conditions = new ArrayList<>();
            }
        }

        return ContactListDto.Response.builder()
                .id(list.getId())
                .name(list.getName())
                .description(list.getDescription())
                .type(Boolean.TRUE.equals(list.getIsDynamic()) ? "smart" : "standard")
                .isSmartList(list.getIsDynamic())
                .subscriberCount(list.getContactCount())
                .conditions(conditions)
                .tags(list.getTags() != null ? new ArrayList<>(list.getTags()) : new ArrayList<>())
                .createdAt(list.getCreatedAt())
                .updatedAt(list.getUpdatedAt())
                .build();
    }

    /**
     * Maps CreateRequest to CreateContactListCommand
     */
    public CreateContactListCommand toCreateCommand(ContactListDto.CreateRequest request) {
        return CreateContactListCommand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isSmartList(Boolean.FALSE)
                .segmentRule(null)
                .tags(request.getTags() != null ? new java.util.HashSet<>(request.getTags()) : null)
                .build();
    }

    /**
     * Maps CreateSmartListRequest to CreateContactListCommand
     */
    public CreateContactListCommand toCreateSmartListCommand(ContactListDto.CreateSmartListRequest request) {
        String segmentRule = null;

        // Serialize conditions to JSON
        if (request.getConditions() != null && !request.getConditions().isEmpty()) {
            try {
                segmentRule = objectMapper.writeValueAsString(request.getConditions());
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize smart list conditions: {}", e.getMessage());
                throw new IllegalArgumentException("Invalid smart list conditions format", e);
            }
        }

        return CreateContactListCommand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isSmartList(Boolean.TRUE)
                .segmentRule(segmentRule)
                .build();
    }

    /**
     * Maps UpdateRequest to UpdateContactListCommand
     */
    public UpdateContactListCommand toUpdateCommand(Long id, ContactListDto.UpdateRequest request) {
        return UpdateContactListCommand.builder()
                .id(id)
                .name(request.getName())
                .description(request.getDescription())
                .isSmartList(request.getIsSmartList())
                .segmentRule(null) // For now, we don't update segment rules
                .tags(request.getTags() != null ? new java.util.HashSet<>(request.getTags()) : null)
                .build();
    }

    /**
     * Calculates statistics for a contact list
     */
    public ContactListDto.StatisticsResponse toStatistics(ContactList list) {
        int totalSubscribers = list.getContactCount();

        // Calculate active subscribers (contacts with activity in last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int activeSubscribers = (int) list.getContacts().stream()
                .filter(contact -> contact.getLastActivityAt() != null)
                .filter(contact -> contact.getLastActivityAt().isAfter(thirtyDaysAgo))
                .count();

        // Calculate average engagement score (lead score)
        double avgEngagementScore = 0.0;
        if (totalSubscribers > 0) {
            avgEngagementScore = list.getContacts().stream()
                    .mapToInt(contact -> contact.getLeadScore() != null ? contact.getLeadScore() : 0)
                    .average()
                    .orElse(0.0);
        }

        // Calculate growth rate (new subscribers in last 30 days vs total)
        long newSubscribers = list.getContacts().stream()
                .filter(contact -> contact.getCreatedAt() != null)
                .filter(contact -> contact.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();

        double growthRate = 0.0;
        if (totalSubscribers > 0) {
            growthRate = (newSubscribers * 100.0) / totalSubscribers;
        }

        // For now, unsubscribedToday is set to 0 as we don't track unsubscribe events yet
        // This could be enhanced by adding an audit table for subscription changes
        int unsubscribedToday = 0;

        return ContactListDto.StatisticsResponse.builder()
                .totalSubscribers(totalSubscribers)
                .activeSubscribers(activeSubscribers)
                .unsubscribedToday(unsubscribedToday)
                .growthRate(growthRate)
                .avgEngagementScore(avgEngagementScore)
                .build();
    }
}
