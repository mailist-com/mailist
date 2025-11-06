package com.mailist.mailist.contact.interfaces.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

public class ContactListDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private String type; // "standard" or "smart"
        private Integer subscriberCount;
        private Boolean isSmartList;
        private List<SmartListCondition> conditions;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "List name is required")
        private String name;
        private String description;
        private String type; // "standard" or "smart"
        private Boolean isSmartList;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateSmartListRequest {
        @NotBlank(message = "List name is required")
        private String name;
        private String description;
        private List<SmartListCondition> conditions;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubscribeRequest {
        private List<Long> contactIds;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubscribeResponse {
        private Integer subscribed;
        private Integer failed;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SmartListCondition {
        private String field; // "tags", "status", "engagementScore", etc.
        private String operator; // "equals", "contains", "greater", "less", etc.
        private String value;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StatisticsResponse {
        private Integer totalSubscribers;
        private Integer activeSubscribers;
        private Integer unsubscribedToday;
        private Double growthRate;
        private Double avgEngagementScore;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GlobalStatisticsResponse {
        private Integer totalLists;
        private Integer activeLists;
        private Integer totalSubscribers;
        private Double averageEngagement;
    }
}
