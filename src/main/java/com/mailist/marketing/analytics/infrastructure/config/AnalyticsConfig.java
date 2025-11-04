package com.mailist.marketing.analytics.infrastructure.config;

import com.mailist.marketing.analytics.application.port.out.AnalyticsDataProvider;
import com.mailist.marketing.analytics.application.port.out.ReportExporter;
import com.mailist.marketing.analytics.application.port.out.ReportRepository;
import com.mailist.marketing.analytics.application.usecase.*;
import com.mailist.marketing.analytics.domain.service.ReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AnalyticsConfig {
    
    @Bean
    public GenerateReportUseCase generateReportUseCase(
            ReportRepository reportRepository,
            AnalyticsDataProvider analyticsDataProvider,
            ReportGenerator reportGenerator) {
        return new GenerateReportUseCase(reportRepository, analyticsDataProvider, reportGenerator);
    }
    
    @Bean
    public ExportReportUseCase exportReportUseCase(
            ReportRepository reportRepository,
            ReportExporter reportExporter) {
        return new ExportReportUseCase(reportRepository, reportExporter);
    }
    
    @Bean
    public GetReportUseCase getReportUseCase(ReportRepository reportRepository) {
        return new GetReportUseCase(reportRepository);
    }
    
    @Bean
    public DeleteReportUseCase deleteReportUseCase(ReportRepository reportRepository) {
        return new DeleteReportUseCase(reportRepository);
    }
}