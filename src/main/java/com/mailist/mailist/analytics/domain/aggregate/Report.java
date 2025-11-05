package com.mailist.mailist.analytics.domain.aggregate;

import com.mailist.mailist.analytics.domain.valueobject.ReportData;
import com.mailist.mailist.analytics.domain.valueobject.ReportFormat;
import com.mailist.mailist.analytics.domain.valueobject.ReportType;
import com.mailist.mailist.shared.domain.aggregate.BaseTenantEntity;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Entity
@Table(name = "reports")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Report extends BaseTenantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;
    
    @Column(name = "entity_id")
    private Long entityId;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "report_metrics", joinColumns = @JoinColumn(name = "report_id"))
    @MapKeyColumn(name = "metric_name")
    @Column(name = "metric_value")
    private Map<String, Double> metrics = new HashMap<>();

    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "report_format")
    private ReportFormat reportFormat;
    
    @Column(name = "generated_by")
    private String generatedBy;
    
    @Column(name = "file_path")
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Embedded
    private ReportData reportData;
    
    @Column(name = "generated_at")
    private LocalDateTime generatedAt;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        generatedAt = LocalDateTime.now();
    }
    
    public void addMetric(String name, Double value) {
        this.metrics.put(name, value);
    }
    
    public Double getMetric(String name) {
        return this.metrics.get(name);
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    // Factory methods
    public static Report createCampaignReport(String name, String description, Long entityId,
                                            ReportFormat format, ReportData data, String generatedBy) {
        return Report.builder()
                .name(name)
                .description(description)
                .reportType(ReportType.CAMPAIGN)
                .entityId(entityId)
                .reportFormat(format)
                .reportData(data)
                .generatedBy(generatedBy)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }
    
    public static Report createContactReport(String name, String description, Long entityId,
                                           ReportFormat format, ReportData data, String generatedBy) {
        return Report.builder()
                .name(name)
                .description(description)
                .reportType(ReportType.CONTACT)
                .entityId(entityId)
                .reportFormat(format)
                .reportData(data)
                .generatedBy(generatedBy)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }
    
    public static Report createAutomationReport(String name, String description, Long entityId,
                                              ReportFormat format, ReportData data, String generatedBy) {
        return Report.builder()
                .name(name)
                .description(description)
                .reportType(ReportType.AUTOMATION)
                .entityId(entityId)
                .reportFormat(format)
                .reportData(data)
                .generatedBy(generatedBy)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }
    
    public static Report createOverallReport(String name, String description,
                                           ReportFormat format, ReportData data, String generatedBy) {
        return Report.builder()
                .name(name)
                .description(description)
                .reportType(ReportType.OVERALL)
                .reportFormat(format)
                .reportData(data)
                .generatedBy(generatedBy)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }
}