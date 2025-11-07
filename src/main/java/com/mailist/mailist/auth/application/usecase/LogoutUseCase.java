package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.infrastructure.repository.RefreshTokenRepository;
import com.mailist.mailist.auth.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
final class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    void execute(final long userId) {
        log.info("Executing logout use case for user ID: {}", userId);

        // Find user
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Revoke all refresh tokens for this user
        refreshTokenRepository.deleteByUser(user);

        log.info("Successfully logged out user: {}", user.getEmail());
    }
}
