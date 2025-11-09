package com.mailist.mailist.auth.interfaces.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponseDto {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String avatar;
    private String role;
    private String phone;
    private String company;
    private String timezone;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private NotificationSettingsDto notifications;
    private UserPreferencesDto preferences;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotificationSettingsDto {
        private Boolean emailNotifications;
        private Boolean campaignUpdates;
        private Boolean automationAlerts;
        private Boolean monthlyReports;
        private Boolean systemUpdates;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserPreferencesDto {
        private String emailSignature;
        private String defaultFromName;
        private String defaultFromEmail;
        private String dateFormat;
        private String timeFormat;
    }
}
