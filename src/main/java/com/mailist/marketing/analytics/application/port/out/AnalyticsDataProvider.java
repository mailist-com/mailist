package com.mailist.marketing.analytics.application.port.out;

import com.mailist.marketing.analytics.domain.valueobject.ReportData;
import com.mailist.marketing.analytics.domain.valueobject.ReportType;

import java.time.LocalDateTime;

public interface AnalyticsDataProvider {
    
    ReportData getCampaignAnalytics(Long campaignId, LocalDateTime startDate, LocalDateTime endDate);
    
    ReportData getContactAnalytics(Long contactId, LocalDateTime startDate, LocalDateTime endDate);
    
    ReportData getAutomationAnalytics(Long automationId, LocalDateTime startDate, LocalDateTime endDate);
    
    ReportData getOverallAnalytics(LocalDateTime startDate, LocalDateTime endDate);
    
    ReportData getCustomAnalytics(ReportType reportType, Long entityId, 
                                 LocalDateTime startDate, LocalDateTime endDate, 
                                 String customFilters);
    
    boolean hasDataForPeriod(ReportType reportType, Long entityId, 
                           LocalDateTime startDate, LocalDateTime endDate);
}