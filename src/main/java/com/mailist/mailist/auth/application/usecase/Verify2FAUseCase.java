package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.usecase.command.Verify2FACommand;
import com.mailist.mailist.auth.application.usecase.dto.Verify2FAResult;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.domain.service.TwoFactorAuthService;
import com.mailist.mailist.auth.infrastructure.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
final class Verify2FAUseCase {

    private final UserRepository userRepository;
    private final TwoFactorAuthService twoFactorAuthService;

    Verify2FAResult execute(final Verify2FACommand command) {
        log.info("Executing 2FA verification for user ID: {}", command.getUserId());

        // Find user
        final User user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if 2FA is enabled for this user
        if (!user.getTwoFactorEnabled()) {
            log.warn("2FA is not enabled for user: {}", user.getEmail());
            throw new IllegalArgumentException("2FA is not enabled for this user");
        }

        // Verify the code
        final boolean isValid = twoFactorAuthService.verifyCode(user, command.getCode());

        if (!isValid) {
            log.warn("Invalid 2FA code for user: {}", user.getEmail());
            throw new IllegalArgumentException("Invalid 2FA code");
        }

        log.info("2FA verification successful for user: {}", user.getEmail());

        return Verify2FAResult.builder()
                .verified(true)
                .build();
    }
}
