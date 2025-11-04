package com.mailist.marketing.analytics.domain.service;

import com.mailist.marketing.analytics.domain.aggregate.Report;
import com.mailist.marketing.analytics.domain.valueobject.ReportData;
import com.mailist.marketing.analytics.domain.valueobject.ReportFormat;
import com.mailist.marketing.analytics.domain.valueobject.ReportType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportGenerator {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    public Report generateCampaignReport(Long campaignId, String campaignName, ReportFormat format, 
                                       ReportData data, String generatedBy) {
        log.info("Generating campaign report for campaign ID: {}", campaignId);
        
        String reportName = String.format("Campaign_Report_%s_%s", 
                campaignName.replaceAll("[^a-zA-Z0-9]", "_"),
                LocalDateTime.now().format(DATE_FORMATTER));
        
        String description = String.format("Analytics report for campaign: %s", campaignName);
        
        return Report.createCampaignReport(reportName, description, campaignId, format, data, generatedBy);
    }
    
    public Report generateContactReport(Long contactId, String contactEmail, ReportFormat format, 
                                      ReportData data, String generatedBy) {
        log.info("Generating contact report for contact ID: {}", contactId);
        
        String reportName = String.format("Contact_Report_%s_%s", 
                contactEmail.replaceAll("[^a-zA-Z0-9]", "_"),
                LocalDateTime.now().format(DATE_FORMATTER));
        
        String description = String.format("Analytics report for contact: %s", contactEmail);
        
        return Report.createContactReport(reportName, description, contactId, format, data, generatedBy);
    }
    
    public Report generateAutomationReport(Long automationId, String automationName, ReportFormat format, 
                                         ReportData data, String generatedBy) {
        log.info("Generating automation report for automation ID: {}", automationId);
        
        String reportName = String.format("Automation_Report_%s_%s", 
                automationName.replaceAll("[^a-zA-Z0-9]", "_"),
                LocalDateTime.now().format(DATE_FORMATTER));
        
        String description = String.format("Analytics report for automation: %s", automationName);
        
        return Report.createAutomationReport(reportName, description, automationId, format, data, generatedBy);
    }
    
    public Report generateOverallReport(ReportFormat format, ReportData data, String generatedBy) {
        log.info("Generating overall analytics report");
        
        String reportName = String.format("Overall_Analytics_Report_%s", 
                LocalDateTime.now().format(DATE_FORMATTER));
        
        String description = "Overall platform analytics and performance metrics";
        
        return Report.createOverallReport(reportName, description, format, data, generatedBy);
    }
    
    public Report generateCustomReport(String name, String description, ReportType type, 
                                     Long entityId, ReportFormat format, ReportData data, String generatedBy) {
        log.info("Generating custom report: {} for type: {}", name, type);
        
        String reportName = String.format("%s_%s", 
                name.replaceAll("[^a-zA-Z0-9]", "_"),
                LocalDateTime.now().format(DATE_FORMATTER));
        
        return switch (type) {
            case CAMPAIGN -> Report.createCampaignReport(reportName, description, entityId, format, data, generatedBy);
            case CONTACT -> Report.createContactReport(reportName, description, entityId, format, data, generatedBy);
            case AUTOMATION -> Report.createAutomationReport(reportName, description, entityId, format, data, generatedBy);
            case OVERALL -> Report.createOverallReport(reportName, description, format, data, generatedBy);
        };
    }
    
    public String generateFileName(String reportName, ReportFormat format) {
        return reportName + format.getFileExtension();
    }
    
    public boolean isValidReportFormat(ReportFormat format) {
        return format != null && (format == ReportFormat.PDF || format == ReportFormat.CSV || 
                                 format == ReportFormat.EXCEL || format == ReportFormat.JSON);
    }
    
    public long estimateReportSize(ReportData data, ReportFormat format) {
        // Basic estimation based on format and data size
        long baseSize = switch (format) {
            case PDF -> 50000; // 50KB base
            case CSV -> 5000;  // 5KB base
            case EXCEL -> 25000; // 25KB base
            case JSON -> 10000; // 10KB base
        };
        
        // Add size based on data complexity
        long dataSize = 0;
        if (data.getTotalSent() != null) dataSize += 100;
        if (data.getTotalContacts() != null) dataSize += 100;
        if (data.getAdditionalData() != null) dataSize += data.getAdditionalData().length();
        
        return baseSize + dataSize;
    }
}