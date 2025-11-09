package com.mailist.mailist.auth.interfaces.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNotificationSettingsRequestDto {

    private Boolean emailNotifications;
    private Boolean campaignUpdates;
    private Boolean automationAlerts;
    private Boolean monthlyReports;
    private Boolean systemUpdates;
}
