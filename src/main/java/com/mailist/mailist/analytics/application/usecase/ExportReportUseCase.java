package com.mailist.mailist.analytics.application.usecase;

import com.mailist.mailist.analytics.application.port.out.ReportExporter;
import com.mailist.mailist.analytics.application.port.out.ReportRepository;
import com.mailist.mailist.analytics.domain.aggregate.Report;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportReportUseCase {
    
    private final ReportRepository reportRepository;
    private final ReportExporter reportExporter;
    
    public ByteArrayOutputStream execute(ExportReportCommand command) {
        log.info("Exporting report ID: {} in format: {} requested by: {}", 
                command.getReportId(), command.getExportFormat(), command.getRequestedBy());
        
        // Find report
        Report report = reportRepository.findById(command.getReportId())
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + command.getReportId()));
        
        // Check if report is expired
        if (report.isExpired()) {
            throw new IllegalStateException("Report has expired and cannot be exported");
        }
        
        // Validate export format
        if (!reportExporter.supportsFormat(command.getExportFormat())) {
            throw new IllegalArgumentException("Export format not supported: " + command.getExportFormat());
        }
        
        // Export report
        ByteArrayOutputStream exportedData = reportExporter.exportReport(report, command.getExportFormat());
        
        log.info("Successfully exported report ID: {} in format: {}", 
                command.getReportId(), command.getExportFormat());
        
        return exportedData;
    }
}