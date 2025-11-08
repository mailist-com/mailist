package com.mailist.mailist.analytics.application.service;

import com.mailist.mailist.analytics.infrastructure.repository.EmailEventRepository;
import com.mailist.mailist.analytics.interfaces.dto.DashboardDto;
import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import com.mailist.mailist.campaign.infrastructure.repository.CampaignRepository;
import com.mailist.mailist.contact.infrastructure.repository.ContactRepository;
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final ContactRepository contactRepository;
    private final CampaignRepository campaignRepository;
    private final EmailEventRepository emailEventRepository;

    public DashboardDto.OverviewStats getOverviewStats() {
        Long tenantId = TenantContext.getOrganizationId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        LocalDateTime sixtyDaysAgo = now.minusDays(60);

        // Total contacts
        Long totalContacts = contactRepository.count();
        Long contactsLastMonth = contactRepository.countByCreatedAtAfter(thirtyDaysAgo);

        // Contacts change calculation
        Long contactsPrevious = contactRepository.countByCreatedAtBefore(thirtyDaysAgo);
        double contactsChange = contactsPrevious > 0
            ? ((double) (totalContacts - contactsPrevious) / contactsPrevious) * 100
            : 0.0;

        // Sent emails last 30 days
        Long sentEmails30Days = emailEventRepository.countSentEmailsByTenantSince(tenantId, thirtyDaysAgo);
        Long sentEmailsPrevious30Days = emailEventRepository.countSentEmailsByTenantSince(tenantId, sixtyDaysAgo) - sentEmails30Days;

        double sentEmailsChange = sentEmailsPrevious30Days > 0
            ? ((double) (sentEmails30Days - sentEmailsPrevious30Days) / sentEmailsPrevious30Days) * 100
            : 0.0;

        // Open rate calculation
        Long opens30Days = emailEventRepository.countOpenedEmailsByTenantSince(tenantId, thirtyDaysAgo);
        Double averageOpenRate = sentEmails30Days > 0
            ? ((double) opens30Days / sentEmails30Days) * 100
            : 0.0;

        Long opensPrevious = emailEventRepository.countOpenedEmailsByTenantSince(tenantId, sixtyDaysAgo) - opens30Days;
        Double previousAverageOpenRate = sentEmailsPrevious30Days > 0
            ? ((double) opensPrevious / sentEmailsPrevious30Days) * 100
            : 0.0;

        double openRateChange = previousAverageOpenRate > 0
            ? ((averageOpenRate - previousAverageOpenRate) / previousAverageOpenRate) * 100
            : 0.0;

        // Click rate calculation
        Long clicks30Days = emailEventRepository.countClickedEmailsByTenantSince(tenantId, thirtyDaysAgo);
        Double averageClickRate = sentEmails30Days > 0
            ? ((double) clicks30Days / sentEmails30Days) * 100
            : 0.0;

        Long clicksPrevious = emailEventRepository.countClickedEmailsByTenantSince(tenantId, sixtyDaysAgo) - clicks30Days;
        Double previousAverageClickRate = sentEmailsPrevious30Days > 0
            ? ((double) clicksPrevious / sentEmailsPrevious30Days) * 100
            : 0.0;

        double clickRateChange = previousAverageClickRate > 0
            ? ((averageClickRate - previousAverageClickRate) / previousAverageClickRate) * 100
            : 0.0;

        return DashboardDto.OverviewStats.builder()
                .totalContacts(totalContacts)
                .contactsLastMonth(contactsLastMonth)
                .contactsChangePercentage(String.format("%.1f%%", Math.abs(contactsChange)))
                .contactsChangeType(contactsChange >= 0 ? "positive" : "negative")
                .sentEmails30Days(sentEmails30Days)
                .sentEmailsPrevious30Days(sentEmailsPrevious30Days)
                .sentEmailsChangePercentage(String.format("%.1f%%", Math.abs(sentEmailsChange)))
                .sentEmailsChangeType(sentEmailsChange >= 0 ? "positive" : "negative")
                .averageOpenRate(Math.round(averageOpenRate * 10.0) / 10.0)
                .previousAverageOpenRate(Math.round(previousAverageOpenRate * 10.0) / 10.0)
                .openRateChangePercentage(String.format("%.1f%%", Math.abs(openRateChange)))
                .openRateChangeType(openRateChange >= 0 ? "positive" : "negative")
                .averageClickRate(Math.round(averageClickRate * 10.0) / 10.0)
                .previousAverageClickRate(Math.round(previousAverageClickRate * 10.0) / 10.0)
                .clickRateChangePercentage(String.format("%.1f%%", Math.abs(clickRateChange)))
                .clickRateChangeType(clickRateChange >= 0 ? "positive" : "negative")
                .build();
    }

    public DashboardDto.RecentCampaigns getRecentCampaigns(int limit) {
        Long tenantId = TenantContext.getOrganizationId();

        // Get recent campaigns
        List<Campaign> campaigns = campaignRepository.findAll(PageRequest.of(0, limit)).getContent();

        // Get email stats by campaign
        Map<String, Long> sentByCampaign = emailEventRepository.countSentByCampaign(tenantId)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));

        Map<String, Long> opensByCampaign = emailEventRepository.countOpensByCampaign(tenantId)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));

        Map<String, Long> clicksByCampaign = emailEventRepository.countClicksByCampaign(tenantId)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<DashboardDto.CampaignStats> campaignStats = campaigns.stream()
                .map(campaign -> {
                    String campaignId = String.valueOf(campaign.getId());
                    Long sent = sentByCampaign.getOrDefault(campaignId, 0L);
                    Long opens = opensByCampaign.getOrDefault(campaignId, 0L);
                    Long clicks = clicksByCampaign.getOrDefault(campaignId, 0L);

                    Double openRate = sent > 0 ? ((double) opens / sent) * 100 : 0.0;
                    Double clickRate = sent > 0 ? ((double) clicks / sent) * 100 : 0.0;

                    return DashboardDto.CampaignStats.builder()
                            .campaignId(campaignId)
                            .campaignName(campaign.getName())
                            .subject(campaign.getSubject())
                            .status(campaign.getStatus().name().toLowerCase())
                            .sent(sent)
                            .opens(opens)
                            .clicks(clicks)
                            .openRate(Math.round(openRate * 10.0) / 10.0)
                            .clickRate(Math.round(clickRate * 10.0) / 10.0)
                            .sentDate(campaign.getSentAt() != null
                                    ? campaign.getSentAt().format(formatter)
                                    : campaign.getScheduledAt() != null
                                            ? campaign.getScheduledAt().format(formatter)
                                            : "-")
                            .build();
                })
                .collect(Collectors.toList());

        return DashboardDto.RecentCampaigns.builder()
                .campaigns(campaignStats)
                .build();
    }

    public DashboardDto.GrowthData getGrowthData(int year) {
        Long tenantId = TenantContext.getOrganizationId();

        // Get contacts by month
        List<Object[]> contactsData = contactRepository.countByMonth(tenantId, year);
        Map<Integer, Long> contactsByMonth = contactsData.stream()
                .collect(Collectors.toMap(
                        arr -> ((Number) arr[0]).intValue(),
                        arr -> (Long) arr[1]
                ));

        // Get sent emails by month
        List<Object[]> sentData = emailEventRepository.countSentByMonth(tenantId, year);
        Map<Integer, Long> sentByMonth = sentData.stream()
                .collect(Collectors.toMap(
                        arr -> ((Number) arr[0]).intValue(),
                        arr -> (Long) arr[1]
                ));

        // Month labels
        String[] monthNames = {"Sty", "Lut", "Mar", "Kwi", "Maj", "Cze",
                               "Lip", "Sie", "Wrz", "Paź", "Lis", "Gru"};

        List<Integer> contactsList = new ArrayList<>();
        List<Integer> sentList = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            contactsList.add(contactsByMonth.getOrDefault(i, 0L).intValue());
            sentList.add(sentByMonth.getOrDefault(i, 0L).intValue());
            labels.add(monthNames[i - 1]);
        }

        return DashboardDto.GrowthData.builder()
                .contactsByMonth(contactsList)
                .sentEmailsByMonth(sentList)
                .monthLabels(labels)
                .build();
    }

    public DashboardDto.ActivityFeed getActivityFeed(int limit) {
        // This is a simplified version - you can extend this with actual activity tracking
        List<DashboardDto.ActivityItem> activities = new ArrayList<>();

        // Get recent campaigns
        List<Campaign> recentCampaigns = campaignRepository.findAll(PageRequest.of(0, 5)).getContent();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Campaign campaign : recentCampaigns) {
            if (campaign.getSentAt() != null) {
                activities.add(DashboardDto.ActivityItem.builder()
                        .type("campaign_sent")
                        .message("Wysłano kampanię: " + campaign.getName())
                        .timestamp(campaign.getSentAt().format(formatter))
                        .icon("lucideSend")
                        .build());
            }
        }

        // Sort by timestamp descending
        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return DashboardDto.ActivityFeed.builder()
                .activities(activities.stream().limit(limit).collect(Collectors.toList()))
                .build();
    }
}
