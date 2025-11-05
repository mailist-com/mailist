package com.mailist.mailist.analytics.application.usecase;

import com.mailist.mailist.analytics.application.port.out.ReportRepository;
import com.mailist.mailist.analytics.domain.aggregate.Report;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetReportUseCase {
    
    private final ReportRepository reportRepository;
    
    public Report execute(GetReportQuery query) {
        log.debug("Retrieving report with ID: {}", query.getReportId());
        
        return reportRepository.findById(query.getReportId())
                .orElseThrow(() -> new IllegalArgumentException("Report not found with ID: " + query.getReportId()));
    }
}