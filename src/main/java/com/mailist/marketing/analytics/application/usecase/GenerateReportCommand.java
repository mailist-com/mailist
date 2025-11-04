package com.mailist.marketing.analytics.application.usecase;

import com.mailist.marketing.analytics.domain.valueobject.ReportFormat;
import com.mailist.marketing.analytics.domain.valueobject.ReportType;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class GenerateReportCommand {
    String name;
    String description;
    ReportType reportType;
    ReportFormat reportFormat;
    Long entityId;
    LocalDateTime startDate;
    LocalDateTime endDate;
    String generatedBy;
    String customFilters;
}