package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.usecase.command.UpdateNotificationSettingsCommand;
import com.mailist.mailist.auth.application.usecase.command.UpdatePreferencesCommand;
import com.mailist.mailist.auth.application.usecase.command.UpdateProfileCommand;
import com.mailist.mailist.auth.application.usecase.dto.UserProfileResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileApplicationService {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final UpdateNotificationSettingsUseCase updateNotificationSettingsUseCase;
    private final UpdatePreferencesUseCase updatePreferencesUseCase;

    public UserProfileResult getUserProfile(final Long userId) {
        return getUserProfileUseCase.execute(userId);
    }

    public UserProfileResult updateProfile(final UpdateProfileCommand command) {
        return updateProfileUseCase.execute(command);
    }

    public UserProfileResult updateNotificationSettings(final UpdateNotificationSettingsCommand command) {
        return updateNotificationSettingsUseCase.execute(command);
    }

    public UserProfileResult updatePreferences(final UpdatePreferencesCommand command) {
        return updatePreferencesUseCase.execute(command);
    }
}
