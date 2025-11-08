package com.mailist.mailist.analytics.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class DashboardDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewStats {
        private Long totalContacts;
        private Long contactsLastMonth;
        private String contactsChangePercentage;
        private String contactsChangeType; // "positive" or "negative"

        private Long sentEmails30Days;
        private Long sentEmailsPrevious30Days;
        private String sentEmailsChangePercentage;
        private String sentEmailsChangeType;

        private Double averageOpenRate;
        private Double previousAverageOpenRate;
        private String openRateChangePercentage;
        private String openRateChangeType;

        private Double averageClickRate;
        private Double previousAverageClickRate;
        private String clickRateChangePercentage;
        private String clickRateChangeType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampaignStats {
        private String campaignId;
        private String campaignName;
        private String subject;
        private String status;
        private Long sent;
        private Long opens;
        private Long clicks;
        private Double openRate;
        private Double clickRate;
        private String sentDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentCampaigns {
        private List<CampaignStats> campaigns;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrowthData {
        private List<Integer> contactsByMonth;
        private List<Integer> sentEmailsByMonth;
        private List<String> monthLabels;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityItem {
        private String type; // "campaign_sent", "contact_added", etc.
        private String message;
        private String timestamp;
        private String icon;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityFeed {
        private List<ActivityItem> activities;
    }
}
