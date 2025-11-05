package com.mailist.mailist.analytics.domain.service;

import com.mailist.mailist.analytics.domain.aggregate.Report;
import com.mailist.mailist.analytics.domain.valueobject.ReportType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AnalyticsService {
    
    public Report generateCampaignReport(Long campaignId, LocalDateTime startDate, LocalDateTime endDate) {
        return Report.builder()
                .name("Campaign Performance Report")
                .reportType(ReportType.CAMPAIGN)
                .entityId(campaignId)
                .periodStart(startDate)
                .periodEnd(endDate)
                .build();
    }
    
    public Report generateAutomationReport(Long automationId, LocalDateTime startDate, LocalDateTime endDate) {
        return Report.builder()
                .name("Automation Performance Report")
                .reportType(ReportType.AUTOMATION)
                .entityId(automationId)
                .periodStart(startDate)
                .periodEnd(endDate)
                .build();
    }
    
    public Report generateContactEngagementReport(LocalDateTime startDate, LocalDateTime endDate) {
        return Report.builder()
                .name("Contact Engagement Report")
                .reportType(ReportType.CONTACT)
                .periodStart(startDate)
                .periodEnd(endDate)
                .build();
    }
    
    public void calculateCampaignMetrics(Report report, Map<String, Object> campaignData) {
        double openRate = calculateOpenRate(campaignData);
        double clickRate = calculateClickRate(campaignData);
        double conversionRate = calculateConversionRate(campaignData);
        double bounceRate = calculateBounceRate(campaignData);
        
        report.addMetric("open_rate", openRate);
        report.addMetric("click_rate", clickRate);
        report.addMetric("conversion_rate", conversionRate);
        report.addMetric("bounce_rate", bounceRate);
    }
    
    public void calculateAutomationMetrics(Report report, Map<String, Object> automationData) {
        double triggerRate = calculateTriggerRate(automationData);
        double completionRate = calculateCompletionRate(automationData);
        double effectivenessScore = calculateEffectivenessScore(automationData);
        
        report.addMetric("trigger_rate", triggerRate);
        report.addMetric("completion_rate", completionRate);
        report.addMetric("effectiveness_score", effectivenessScore);
    }
    
    public void calculateContactMetrics(Report report, Map<String, Object> contactData) {
        double engagementRate = calculateEngagementRate(contactData);
        double growthRate = calculateGrowthRate(contactData);
        double churnRate = calculateChurnRate(contactData);
        
        report.addMetric("engagement_rate", engagementRate);
        report.addMetric("growth_rate", growthRate);
        report.addMetric("churn_rate", churnRate);
    }
    
    private double calculateOpenRate(Map<String, Object> data) {
        Integer sent = (Integer) data.getOrDefault("sent", 0);
        Integer opened = (Integer) data.getOrDefault("opened", 0);
        return sent > 0 ? (double) opened / sent * 100 : 0.0;
    }
    
    private double calculateClickRate(Map<String, Object> data) {
        Integer sent = (Integer) data.getOrDefault("sent", 0);
        Integer clicked = (Integer) data.getOrDefault("clicked", 0);
        return sent > 0 ? (double) clicked / sent * 100 : 0.0;
    }
    
    private double calculateConversionRate(Map<String, Object> data) {
        Integer sent = (Integer) data.getOrDefault("sent", 0);
        Integer converted = (Integer) data.getOrDefault("converted", 0);
        return sent > 0 ? (double) converted / sent * 100 : 0.0;
    }
    
    private double calculateBounceRate(Map<String, Object> data) {
        Integer sent = (Integer) data.getOrDefault("sent", 0);
        Integer bounced = (Integer) data.getOrDefault("bounced", 0);
        return sent > 0 ? (double) bounced / sent * 100 : 0.0;
    }
    
    private double calculateTriggerRate(Map<String, Object> data) {
        Integer eligible = (Integer) data.getOrDefault("eligible", 0);
        Integer triggered = (Integer) data.getOrDefault("triggered", 0);
        return eligible > 0 ? (double) triggered / eligible * 100 : 0.0;
    }
    
    private double calculateCompletionRate(Map<String, Object> data) {
        Integer started = (Integer) data.getOrDefault("started", 0);
        Integer completed = (Integer) data.getOrDefault("completed", 0);
        return started > 0 ? (double) completed / started * 100 : 0.0;
    }
    
    private double calculateEffectivenessScore(Map<String, Object> data) {
        double triggerRate = calculateTriggerRate(data);
        double completionRate = calculateCompletionRate(data);
        return (triggerRate + completionRate) / 2;
    }
    
    private double calculateEngagementRate(Map<String, Object> data) {
        Integer totalContacts = (Integer) data.getOrDefault("totalContacts", 0);
        Integer activeContacts = (Integer) data.getOrDefault("activeContacts", 0);
        return totalContacts > 0 ? (double) activeContacts / totalContacts * 100 : 0.0;
    }
    
    private double calculateGrowthRate(Map<String, Object> data) {
        Integer previousCount = (Integer) data.getOrDefault("previousCount", 0);
        Integer currentCount = (Integer) data.getOrDefault("currentCount", 0);
        return previousCount > 0 ? (double) (currentCount - previousCount) / previousCount * 100 : 0.0;
    }
    
    private double calculateChurnRate(Map<String, Object> data) {
        Integer startCount = (Integer) data.getOrDefault("startCount", 0);
        Integer churnedCount = (Integer) data.getOrDefault("churnedCount", 0);
        return startCount > 0 ? (double) churnedCount / startCount * 100 : 0.0;
    }
}