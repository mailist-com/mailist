package com.mailist.marketing.analytics.infrastructure.repository;

import com.mailist.marketing.analytics.application.port.out.ReportRepository;
import com.mailist.marketing.analytics.domain.aggregate.Report;
import com.mailist.marketing.analytics.domain.valueobject.ReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepository {
    
    private final ReportJpaRepository jpaRepository;
    
    @Override
    public Report save(Report report) {
        return jpaRepository.save(report);
    }
    
    @Override
    public Optional<Report> findById(Long id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public List<Report> findByReportType(ReportType reportType) {
        return jpaRepository.findByReportType(reportType);
    }
    
    @Override
    public List<Report> findByEntityId(Long entityId) {
        return jpaRepository.findByEntityId(entityId);
    }
    
    @Override
    public List<Report> findByReportTypeAndEntityId(ReportType reportType, Long entityId) {
        return jpaRepository.findByReportTypeAndEntityId(reportType, entityId);
    }
    
    @Override
    public List<Report> findByGeneratedBy(String generatedBy) {
        return jpaRepository.findByGeneratedBy(generatedBy);
    }
    
    @Override
    public List<Report> findByGeneratedAtBetween(LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByGeneratedAtBetween(start, end);
    }
    
    @Override
    public List<Report> findExpiredReports() {
        return jpaRepository.findExpiredReports(LocalDateTime.now());
    }
    
    @Override
    public Page<Report> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }
    
    @Override
    public Page<Report> findByReportType(ReportType reportType, Pageable pageable) {
        return jpaRepository.findByReportType(reportType, pageable);
    }
    
    @Override
    public Page<Report> findByGeneratedBy(String generatedBy, Pageable pageable) {
        return jpaRepository.findByGeneratedBy(generatedBy, pageable);
    }
    
    @Override
    public void delete(Report report) {
        jpaRepository.delete(report);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public void deleteExpiredReports() {
        jpaRepository.deleteExpiredReports(LocalDateTime.now());
    }
    
    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
    
    @Override
    public long count() {
        return jpaRepository.count();
    }
    
    @Override
    public long countByReportType(ReportType reportType) {
        return jpaRepository.countByReportType(reportType);
    }
    
    @Override
    public long countByGeneratedBy(String generatedBy) {
        return jpaRepository.countByGeneratedBy(generatedBy);
    }
}