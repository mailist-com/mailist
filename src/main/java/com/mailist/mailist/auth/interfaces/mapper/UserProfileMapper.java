package com.mailist.mailist.auth.interfaces.mapper;

import com.mailist.mailist.auth.application.usecase.command.UpdateNotificationSettingsCommand;
import com.mailist.mailist.auth.application.usecase.command.UpdatePreferencesCommand;
import com.mailist.mailist.auth.application.usecase.command.UpdateProfileCommand;
import com.mailist.mailist.auth.application.usecase.dto.UserProfileResult;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.domain.aggregate.UserPreferences;
import com.mailist.mailist.auth.interfaces.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    // Command mappings
    @Mapping(target = "userId", ignore = true)
    UpdateProfileCommand toCommand(UpdateProfileRequestDto dto);

    @Mapping(target = "userId", ignore = true)
    UpdateNotificationSettingsCommand toNotificationCommand(UpdateNotificationSettingsRequestDto dto);

    @Mapping(target = "userId", ignore = true)
    UpdatePreferencesCommand toPreferencesCommand(UpdatePreferencesRequestDto dto);

    // Response mapping
    default UserProfileResponseDto toResponse(UserProfileResult result) {
        User user = result.user();
        UserPreferences preferences = user.getPreferences();

        UserProfileResponseDto.NotificationSettingsDto notificationSettings = UserProfileResponseDto.NotificationSettingsDto.builder()
                .emailNotifications(preferences != null ? preferences.getEmailNotifications() : true)
                .campaignUpdates(preferences != null ? preferences.getCampaignUpdates() : true)
                .automationAlerts(preferences != null ? preferences.getAutomationAlerts() : true)
                .monthlyReports(preferences != null ? preferences.getMonthlyReports() : true)
                .systemUpdates(preferences != null ? preferences.getSystemUpdates() : false)
                .build();

        UserProfileResponseDto.UserPreferencesDto userPreferences = UserProfileResponseDto.UserPreferencesDto.builder()
                .emailSignature(preferences != null ? preferences.getEmailSignature() : null)
                .defaultFromName(preferences != null ? preferences.getDefaultFromName() : null)
                .defaultFromEmail(preferences != null ? preferences.getDefaultFromEmail() : null)
                .dateFormat(preferences != null ? preferences.getDateFormat() : "DD.MM.YYYY")
                .timeFormat(preferences != null ? preferences.getTimeFormat() : "24h")
                .build();

        return UserProfileResponseDto.builder()
                .id(user.getId() != null ? user.getId().toString() : null)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatar(user.getAvatar())
                .role(getPrimaryRole(user.getRoles()))
                .phone(user.getPhone())
                .company(user.getCompany())
                .timezone(user.getTimezone())
                .language(user.getLanguage())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .notifications(notificationSettings)
                .preferences(userPreferences)
                .build();
    }

    default String getPrimaryRole(Set<User.Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return "user";
        }

        // Priority order: OWNER > ADMIN > USER
        if (roles.contains(User.Role.OWNER)) {
            return "owner";
        }
        if (roles.contains(User.Role.ADMIN)) {
            return "admin";
        }
        return "user";
    }
}
