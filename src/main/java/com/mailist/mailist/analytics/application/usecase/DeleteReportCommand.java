package com.mailist.mailist.analytics.application.usecase;

import lombok.Value;

@Value
public class DeleteReportCommand {
    Long reportId;
    String deletedBy;
}