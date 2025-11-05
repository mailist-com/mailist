package com.mailist.mailist.analytics.application.usecase;

import com.mailist.mailist.analytics.domain.valueobject.ReportFormat;
import lombok.Value;

@Value
public class ExportReportCommand {
    Long reportId;
    ReportFormat exportFormat;
    String requestedBy;
}