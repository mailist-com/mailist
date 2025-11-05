package com.mailist.mailist.analytics.infrastructure.repository;

import com.mailist.mailist.analytics.application.port.out.ReportRepository;
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
public interface ReportJpaRepository extends JpaRepository<Report, Long>, ReportRepository {

    @Override
    List<Report> findByReportType(ReportType reportType);

    @Override
    List<Report> findByEntityId(Long entityId);

    @Override
    List<Report> findByReportTypeAndEntityId(ReportType reportType, Long entityId);

    @Override
    List<Report> findByGeneratedBy(String generatedBy);

    @Override
    List<Report> findByGeneratedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM Report r WHERE r.expiresAt IS NOT NULL AND r.expiresAt < :now")
    List<Report> findExpiredReportsInternal(@Param("now") LocalDateTime now);

    @Override
    default List<Report> findExpiredReports() {
        return findExpiredReportsInternal(LocalDateTime.now());
    }

    @Override
    Page<Report> findByReportType(ReportType reportType, Pageable pageable);

    @Override
    Page<Report> findByGeneratedBy(String generatedBy, Pageable pageable);

    @Modifying
    @Query("DELETE FROM Report r WHERE r.expiresAt IS NOT NULL AND r.expiresAt < :now")
    void deleteExpiredReportsInternal(@Param("now") LocalDateTime now);

    @Override
    default void deleteExpiredReports() {
        deleteExpiredReportsInternal(LocalDateTime.now());
    }

    @Override
    long countByReportType(ReportType reportType);

    @Override
    long countByGeneratedBy(String generatedBy);
}