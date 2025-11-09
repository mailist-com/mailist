package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.usecase.command.UpdateProfileCommand;
import com.mailist.mailist.auth.application.usecase.dto.UserProfileResult;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
final class UpdateProfileUseCase {

    private final UserRepository userRepository;

    UserProfileResult execute(final UpdateProfileCommand command) {
        log.info("Updating profile for user ID: {}", command.getUserId());

        final User user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.updateProfile(
                command.getFirstName(),
                command.getLastName(),
                command.getPhone(),
                command.getCompany(),
                command.getTimezone(),
                command.getLanguage()
        );

        userRepository.save(user);

        log.info("Profile updated successfully for user ID: {}", command.getUserId());

        return UserProfileResult.builder()
                .user(user)
                .build();
    }
}
