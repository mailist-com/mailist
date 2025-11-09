package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.usecase.dto.UserProfileResult;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
final class GetUserProfileUseCase {

    private final UserRepository userRepository;

    UserProfileResult execute(final Long userId) {
        log.info("Fetching user profile for user ID: {}", userId);

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Ensure preferences are initialized
        if (user.getPreferences() == null) {
            user.initializePreferences();
            userRepository.save(user);
        }

        return UserProfileResult.builder()
                .user(user)
                .build();
    }
}
