package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.usecase.command.UpdateNotificationSettingsCommand;
import com.mailist.mailist.auth.application.usecase.dto.UserProfileResult;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.domain.aggregate.UserPreferences;
import com.mailist.mailist.auth.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
final class UpdateNotificationSettingsUseCase {

    private final UserRepository userRepository;

    UserProfileResult execute(final UpdateNotificationSettingsCommand command) {
        log.info("Updating notification settings for user ID: {}", command.getUserId());

        final User user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Ensure preferences exist
        if (user.getPreferences() == null) {
            user.initializePreferences();
        }

        final UserPreferences preferences = user.getPreferences();

        if (command.getEmailNotifications() != null) {
            preferences.setEmailNotifications(command.getEmailNotifications());
        }
        if (command.getCampaignUpdates() != null) {
            preferences.setCampaignUpdates(command.getCampaignUpdates());
        }
        if (command.getAutomationAlerts() != null) {
            preferences.setAutomationAlerts(command.getAutomationAlerts());
        }
        if (command.getMonthlyReports() != null) {
            preferences.setMonthlyReports(command.getMonthlyReports());
        }
        if (command.getSystemUpdates() != null) {
            preferences.setSystemUpdates(command.getSystemUpdates());
        }

        userRepository.save(user);

        log.info("Notification settings updated successfully for user ID: {}", command.getUserId());

        return UserProfileResult.builder()
                .user(user)
                .build();
    }
}
