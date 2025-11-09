package com.mailist.mailist.auth.application.usecase.command;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UpdateNotificationSettingsCommand {
    private Long userId;
    private Boolean emailNotifications;
    private Boolean campaignUpdates;
    private Boolean automationAlerts;
    private Boolean monthlyReports;
    private Boolean systemUpdates;
}
