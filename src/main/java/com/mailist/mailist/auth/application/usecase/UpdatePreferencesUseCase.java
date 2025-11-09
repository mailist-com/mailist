package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.usecase.command.UpdatePreferencesCommand;
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
final class UpdatePreferencesUseCase {

    private final UserRepository userRepository;

    UserProfileResult execute(final UpdatePreferencesCommand command) {
        log.info("Updating preferences for user ID: {}", command.getUserId());

        final User user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Ensure preferences exist
        if (user.getPreferences() == null) {
            user.initializePreferences();
        }

        final UserPreferences preferences = user.getPreferences();

        if (command.getDefaultFromName() != null) {
            preferences.setDefaultFromName(command.getDefaultFromName());
        }
        if (command.getDefaultFromEmail() != null) {
            preferences.setDefaultFromEmail(command.getDefaultFromEmail());
        }
        if (command.getEmailSignature() != null) {
            preferences.setEmailSignature(command.getEmailSignature());
        }
        if (command.getDateFormat() != null) {
            preferences.setDateFormat(command.getDateFormat());
        }
        if (command.getTimeFormat() != null) {
            preferences.setTimeFormat(command.getTimeFormat());
        }

        userRepository.save(user);

        log.info("Preferences updated successfully for user ID: {}", command.getUserId());

        return UserProfileResult.builder()
                .user(user)
                .build();
    }
}
