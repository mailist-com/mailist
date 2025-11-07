package com.mailist.mailist.analytics.infrastructure.provider;

import com.mailist.mailist.analytics.application.port.out.AnalyticsDataProvider;
import com.mailist.mailist.analytics.domain.valueobject.ReportData;
import com.mailist.mailist.analytics.domain.valueobject.ReportType;
import com.mailist.mailist.automation.infrastructure.repository.AutomationRuleRepository;
import com.mailist.mailist.campaign.infrastructure.repository.CampaignRepository;
import com.mailist.mailist.contact.infrastructure.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DefaultAnalyticsDataProvider implements AnalyticsDataProvider {
    
    private final CampaignRepository campaignRepository;
    private final ContactRepository contactRepository;
    private final AutomationRuleRepository automationRuleRepository;
    
    @Override
    public ReportData getCampaignAnalytics(Long campaignId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Gathering campaign analytics for campaign ID: {} from {} to {}", campaignId, startDate, endDate);
        
        // In a real implementation, these would query actual metrics tables
        // For now, we'll return mock data
        return ReportData.createCampaignData(
                1000L,  // sent
                950L,   // delivered
                380L,   // opened
                76L,    // clicked
                50L,    // bounced
                5L,     // unsubscribed
                startDate,
                endDate
        );
    }
    
    @Override
    public ReportData getContactAnalytics(Long contactId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Gathering contact analytics for contact ID: {} from {} to {}", contactId, startDate, endDate);
        
        // Mock data for contact-specific analytics
        return ReportData.createCampaignData(
                25L,    // emails received
                24L,    // delivered
                18L,    // opened
                8L,     // clicked
                1L,     // bounced
                0L,     // unsubscribed
                startDate,
                endDate
        );
    }
    
    @Override
    public ReportData getAutomationAnalytics(Long automationId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Gathering automation analytics for automation ID: {} from {} to {}", automationId, startDate, endDate);
        
        // Mock data for automation-specific analytics
        return ReportData.createCampaignData(
                500L,   // triggered
                475L,   // executed successfully
                190L,   // emails opened
                38L,    // emails clicked
                25L,    // failed executions
                2L,     // unsubscribed
                startDate,
                endDate
        );
    }
    
    @Override
    public ReportData getOverallAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Gathering overall analytics from {} to {}", startDate, endDate);
        
        // In a real implementation, these would aggregate from all sources
        long totalContacts = contactRepository.count();
        long activeCampaigns = campaignRepository.count(); // Would filter by active status
        long activeAutomations = automationRuleRepository.countActive();
        
        return ReportData.createOverallData(
                totalContacts,
                activeCampaigns,
                activeAutomations,
                5000L,  // total sent
                4750L,  // total delivered
                1900L,  // total opened
                380L,   // total clicked
                250L,   // total bounced
                25L,    // total unsubscribed
                startDate,
                endDate
        );
    }
    
    @Override
    public ReportData getCustomAnalytics(ReportType reportType, Long entityId, 
                                       LocalDateTime startDate, LocalDateTime endDate, 
                                       String customFilters) {
        log.info("Gathering custom analytics for type: {} entity: {} from {} to {} with filters: {}", 
                reportType, entityId, startDate, endDate, customFilters);
        
        // Delegate to specific analytics methods based on type
        return switch (reportType) {
            case CAMPAIGN -> getCampaignAnalytics(entityId, startDate, endDate);
            case CONTACT -> getContactAnalytics(entityId, startDate, endDate);
            case AUTOMATION -> getAutomationAnalytics(entityId, startDate, endDate);
            case OVERALL -> getOverallAnalytics(startDate, endDate);
        };
    }
    
    @Override
    public boolean hasDataForPeriod(ReportType reportType, Long entityId, 
                                  LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Checking data availability for type: {} entity: {} from {} to {}", 
                reportType, entityId, startDate, endDate);
        
        // In a real implementation, this would check if metrics tables have data for the period
        // For now, we'll assume data is always available if the entity exists
        return switch (reportType) {
            case CAMPAIGN -> entityId == null || campaignRepository.existsById(entityId);
            case CONTACT -> entityId == null || contactRepository.existsById(entityId);
            case AUTOMATION -> entityId == null || automationRuleRepository.existsById(entityId);
            case OVERALL -> true; // Overall reports don't depend on specific entities
        };
    }
}