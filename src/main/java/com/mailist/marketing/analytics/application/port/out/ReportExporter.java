package com.mailist.marketing.analytics.application.port.out;

import com.mailist.marketing.analytics.domain.aggregate.Report;
import com.mailist.marketing.analytics.domain.valueobject.ReportFormat;

import java.io.ByteArrayOutputStream;

public interface ReportExporter {
    
    ByteArrayOutputStream exportReport(Report report, ReportFormat format);
    
    boolean supportsFormat(ReportFormat format);
    
    String getContentType(ReportFormat format);
    
    long estimateFileSize(Report report, ReportFormat format);
}