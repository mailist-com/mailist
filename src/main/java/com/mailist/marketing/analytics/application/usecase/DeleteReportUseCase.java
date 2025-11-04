package com.mailist.marketing.analytics.application.usecase;

import com.mailist.marketing.analytics.application.port.out.ReportRepository;
import com.mailist.marketing.analytics.domain.aggregate.Report;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteReportUseCase {
    
    private final ReportRepository reportRepository;
    
    @Transactional
    public void execute(DeleteReportCommand command) {
        log.info("Deleting report ID: {} requested by: {}", command.getReportId(), command.getDeletedBy());
        
        // Verify report exists
        Report report = reportRepository.findById(command.getReportId())
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + command.getReportId()));
        
        // Delete report
        reportRepository.delete(report);
        
        log.info("Successfully deleted report ID: {}", command.getReportId());
    }
}