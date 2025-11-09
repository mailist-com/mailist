package com.mailist.mailist.analytics.application.service;

import com.mailist.mailist.analytics.infrastructure.repository.EmailEventRepository;
import com.mailist.mailist.analytics.interfaces.dto.DashboardDto;
import com.mailist.mailist.automation.infrastructure.repository.AutomationRuleRepository;
import com.mailist.mailist.auth.infrastructure.repository.UserRepository;
import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import com.mailist.mailist.campaign.infrastructure.repository.CampaignRepository;
import com.mailist.mailist.contact.infrastructure.repository.ContactRepository;
import com.mailist.mailist.shared.domain.aggregate.Organization;
import com.mailist.mailist.shared.infrastructure.repository.OrganizationJpaRepository;
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
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
    private final UserRepository userRepository;
    private final AutomationRuleRepository automationRuleRepository;
    private final OrganizationJpaRepository organizationJpaRepository;

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

    public DashboardDto.UsageStatistics getUsageStatistics() {
        Long tenantId = TenantContext.getOrganizationId();
        LocalDateTime now = LocalDateTime.now();

        // Get organization to retrieve limits
        Organization organization = ((com.mailist.mailist.shared.application.port.out.OrganizationRepository) organizationJpaRepository).findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        // Get current month start and end
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        // Previous month for trend calculation
        YearMonth previousMonth = currentMonth.minusMonths(1);
        LocalDateTime prevMonthStart = previousMonth.atDay(1).atStartOfDay();
        LocalDateTime prevMonthEnd = previousMonth.atEndOfMonth().atTime(23, 59, 59);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        // Period
        DashboardDto.UsagePeriod period = DashboardDto.UsagePeriod.builder()
                .start(monthStart.format(formatter))
                .end(monthEnd.format(formatter))
                .build();

        // Contacts
        Long totalContacts = contactRepository.count();
        Long contactsLimit = organization.getContactLimit().longValue();
        Double contactsPercentage = contactsLimit > 0 ? (totalContacts.doubleValue() / contactsLimit) * 100 : 0.0;

        Long prevMonthContacts = contactRepository.countByCreatedAtBefore(prevMonthEnd);
        Double contactsTrend = prevMonthContacts > 0
            ? ((totalContacts.doubleValue() - prevMonthContacts) / prevMonthContacts) * 100
            : 0.0;

        DashboardDto.UsageMetric contacts = DashboardDto.UsageMetric.builder()
                .current(totalContacts)
                .limit(contactsLimit)
                .percentage(Math.round(contactsPercentage * 100.0) / 100.0)
                .trend(Math.round(contactsTrend * 100.0) / 100.0)
                .build();

        // Emails sent this month
        Long emailsSentThisMonth = emailEventRepository.countSentEmailsByTenantSince(tenantId, monthStart);
        Long emailsSentPrevMonth = emailEventRepository.countSentEmailsByTenantSince(tenantId, prevMonthStart);
        if (emailsSentPrevMonth > emailsSentThisMonth) {
            emailsSentPrevMonth = emailsSentPrevMonth - emailsSentThisMonth;
        }

        // Using campaign limit as emails limit (can be adjusted based on plan)
        Long emailsLimit = organization.getCampaignLimit().longValue() * 1000L; // Example: 1000 emails per campaign
        Double emailsPercentage = emailsLimit > 0 ? (emailsSentThisMonth.doubleValue() / emailsLimit) * 100 : 0.0;
        Double emailsTrend = emailsSentPrevMonth > 0
            ? ((emailsSentThisMonth.doubleValue() - emailsSentPrevMonth) / emailsSentPrevMonth) * 100
            : 0.0;

        DashboardDto.UsageMetric emails = DashboardDto.UsageMetric.builder()
                .current(emailsSentThisMonth)
                .limit(emailsLimit)
                .percentage(Math.round(emailsPercentage * 100.0) / 100.0)
                .trend(Math.round(emailsTrend * 100.0) / 100.0)
                .build();

        // Users (team members)
        Integer totalUsers = userRepository.countActiveByTenantId(tenantId);
        Long usersLimit = 5L; // Default limit, can be adjusted based on plan
        Double usersPercentage = usersLimit > 0 ? (totalUsers.doubleValue() / usersLimit) * 100 : 0.0;

        DashboardDto.UsageMetric users = DashboardDto.UsageMetric.builder()
                .current(totalUsers.longValue())
                .limit(usersLimit)
                .percentage(Math.round(usersPercentage * 100.0) / 100.0)
                .trend(0.0) // Can be calculated if needed
                .build();

        // Automations
        Long totalAutomations = automationRuleRepository.countActive();
        Long automationsLimit = organization.getAutomationLimit().longValue();
        Double automationsPercentage = automationsLimit > 0 ? (totalAutomations.doubleValue() / automationsLimit) * 100 : 0.0;

        DashboardDto.UsageMetric automations = DashboardDto.UsageMetric.builder()
                .current(totalAutomations)
                .limit(automationsLimit)
                .percentage(Math.round(automationsPercentage * 100.0) / 100.0)
                .trend(0.0) // Can be calculated if needed
                .build();

        // Templates (using campaigns as templates)
        Long totalCampaigns = campaignRepository.count();
        Long templatesLimit = -1L; // Unlimited
        Double templatesPercentage = 0.0;

        DashboardDto.UsageMetric templates = DashboardDto.UsageMetric.builder()
                .current(totalCampaigns)
                .limit(templatesLimit)
                .percentage(templatesPercentage)
                .trend(0.0)
                .build();

        // API Calls - placeholder (would need ApiKeyActivity tracking)
        Long apiCallsThisMonth = 0L;
        Long apiCallsLimit = 10000L;
        Double apiCallsPercentage = 0.0;

        DashboardDto.UsageMetric apiCalls = DashboardDto.UsageMetric.builder()
                .current(apiCallsThisMonth)
                .limit(apiCallsLimit)
                .percentage(apiCallsPercentage)
                .trend(0.0)
                .build();

        // Storage - placeholder
        DashboardDto.StorageMetric storage = DashboardDto.StorageMetric.builder()
                .current(0L)
                .limit(1000L)
                .percentage(0.0)
                .unit("MB")
                .trend(0.0)
                .build();

        return DashboardDto.UsageStatistics.builder()
                .period(period)
                .contacts(contacts)
                .emails(emails)
                .users(users)
                .automations(automations)
                .templates(templates)
                .apiCalls(apiCalls)
                .storage(storage)
                .build();
    }

    public DashboardDto.UsageAlerts getUsageAlerts() {
        Long tenantId = TenantContext.getOrganizationId();

        Organization organization = ((com.mailist.mailist.shared.application.port.out.OrganizationRepository) organizationJpaRepository).findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        List<DashboardDto.UsageAlert> alerts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        // Check contacts usage
        Long totalContacts = contactRepository.count();
        Long contactsLimit = organization.getContactLimit().longValue();
        Double contactsPercentage = contactsLimit > 0 ? (totalContacts.doubleValue() / contactsLimit) * 100 : 0.0;

        if (contactsPercentage >= 80) {
            String severity = contactsPercentage >= 90 ? "critical" : "warning";
            alerts.add(DashboardDto.UsageAlert.builder()
                    .id("alert_contacts")
                    .type("contacts")
                    .threshold(contactsPercentage >= 90 ? 90 : 80)
                    .current(totalContacts)
                    .limit(contactsLimit)
                    .message(String.format("Wykorzystujesz %.0f%% limitu kontaktów", contactsPercentage))
                    .severity(severity)
                    .createdAt(LocalDateTime.now().format(formatter))
                    .build());
        }

        // Check automations usage
        Long totalAutomations = automationRuleRepository.countActive();
        Long automationsLimit = organization.getAutomationLimit().longValue();
        Double automationsPercentage = automationsLimit > 0 ? (totalAutomations.doubleValue() / automationsLimit) * 100 : 0.0;

        if (automationsPercentage >= 80) {
            String severity = automationsPercentage >= 90 ? "critical" : "warning";
            alerts.add(DashboardDto.UsageAlert.builder()
                    .id("alert_automations")
                    .type("automations")
                    .threshold(automationsPercentage >= 90 ? 90 : 80)
                    .current(totalAutomations)
                    .limit(automationsLimit)
                    .message(String.format("Wykorzystujesz %.0f%% limitu automatyzacji", automationsPercentage))
                    .severity(severity)
                    .createdAt(LocalDateTime.now().format(formatter))
                    .build());
        }

        return DashboardDto.UsageAlerts.builder()
                .alerts(alerts)
                .build();
    }
}
