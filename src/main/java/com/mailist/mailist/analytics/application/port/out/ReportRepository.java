package com.mailist.mailist.analytics.application.port.out;

import com.mailist.mailist.analytics.domain.aggregate.Report;
import com.mailist.mailist.analytics.domain.valueobject.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReportRepository {
    
    Report save(Report report);
    
    Optional<Report> findById(Long id);
    
    List<Report> findByReportType(ReportType reportType);
    
    List<Report> findByEntityId(Long entityId);
    
    List<Report> findByReportTypeAndEntityId(ReportType reportType, Long entityId);
    
    List<Report> findByGeneratedBy(String generatedBy);
    
    List<Report> findByGeneratedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<Report> findExpiredReports();
    
    Page<Report> findAll(Pageable pageable);
    
    Page<Report> findByReportType(ReportType reportType, Pageable pageable);
    
    Page<Report> findByGeneratedBy(String generatedBy, Pageable pageable);
    
    void delete(Report report);
    
    void deleteById(Long id);
    
    void deleteExpiredReports();
    
    boolean existsById(Long id);
    
    long count();
    
    long countByReportType(ReportType reportType);
    
    long countByGeneratedBy(String generatedBy);
}