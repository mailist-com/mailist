package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.port.out.RefreshTokenRepository;
import com.mailist.mailist.auth.application.port.out.UserRepository;
import com.mailist.mailist.auth.domain.aggregate.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public void execute(Long userId) {
        log.info("Executing logout use case for user ID: {}", userId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Revoke all refresh tokens for this user
        refreshTokenRepository.deleteByUser(user);

        log.info("Successfully logged out user: {}", user.getEmail());
    }
}
