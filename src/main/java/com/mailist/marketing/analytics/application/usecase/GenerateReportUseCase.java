package com.mailist.marketing.analytics.application.usecase;

import com.mailist.marketing.analytics.application.port.out.AnalyticsDataProvider;
import com.mailist.marketing.analytics.application.port.out.ReportRepository;
import com.mailist.marketing.analytics.domain.aggregate.Report;
import com.mailist.marketing.analytics.domain.service.ReportGenerator;
import com.mailist.marketing.analytics.domain.valueobject.ReportData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateReportUseCase {
    
    private final ReportRepository reportRepository;
    private final AnalyticsDataProvider analyticsDataProvider;
    private final ReportGenerator reportGenerator;
    
    @Transactional
    public Report execute(GenerateReportCommand command) {
        log.info("Generating report: {} of type: {} for entity: {}", 
                command.getName(), command.getReportType(), command.getEntityId());
        
        // Validate data availability
        if (!analyticsDataProvider.hasDataForPeriod(command.getReportType(), command.getEntityId(), 
                command.getStartDate(), command.getEndDate())) {
            throw new IllegalArgumentException("No data available for the specified period");
        }
        
        // Gather analytics data
        ReportData reportData = switch (command.getReportType()) {
            case CAMPAIGN -> analyticsDataProvider.getCampaignAnalytics(
                    command.getEntityId(), command.getStartDate(), command.getEndDate());
            case CONTACT -> analyticsDataProvider.getContactAnalytics(
                    command.getEntityId(), command.getStartDate(), command.getEndDate());
            case AUTOMATION -> analyticsDataProvider.getAutomationAnalytics(
                    command.getEntityId(), command.getStartDate(), command.getEndDate());
            case OVERALL -> analyticsDataProvider.getOverallAnalytics(
                    command.getStartDate(), command.getEndDate());
        };
        
        // Generate report
        Report report = reportGenerator.generateCustomReport(
                command.getName(),
                command.getDescription(),
                command.getReportType(),
                command.getEntityId(),
                command.getReportFormat(),
                reportData,
                command.getGeneratedBy()
        );
        
        // Save report
        Report savedReport = reportRepository.save(report);
        
        log.info("Successfully generated report with ID: {}", savedReport.getId());
        return savedReport;
    }
}