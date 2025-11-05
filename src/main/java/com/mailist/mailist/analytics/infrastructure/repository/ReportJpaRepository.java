package com.mailist.mailist.analytics.infrastructure.repository;

import com.mailist.mailist.analytics.domain.aggregate.Report;
import com.mailist.mailist.analytics.domain.valueobject.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportJpaRepository extends JpaRepository<Report, Long> {
    
    List<Report> findByReportType(ReportType reportType);
    
    List<Report> findByEntityId(Long entityId);
    
    List<Report> findByReportTypeAndEntityId(ReportType reportType, Long entityId);
    
    List<Report> findByGeneratedBy(String generatedBy);
    
    List<Report> findByGeneratedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT r FROM Report r WHERE r.expiresAt IS NOT NULL AND r.expiresAt < :now")
    List<Report> findExpiredReports(@Param("now") LocalDateTime now);
    
    Page<Report> findByReportType(ReportType reportType, Pageable pageable);
    
    Page<Report> findByGeneratedBy(String generatedBy, Pageable pageable);
    
    @Modifying
    @Query("DELETE FROM Report r WHERE r.expiresAt IS NOT NULL AND r.expiresAt < :now")
    void deleteExpiredReports(@Param("now") LocalDateTime now);
    
    long countByReportType(ReportType reportType);
    
    long countByGeneratedBy(String generatedBy);
}