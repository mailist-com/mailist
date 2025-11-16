package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.usecase.command.ChangePasswordCommand;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.infrastructure.repository.RefreshTokenRepository;
import com.mailist.mailist.auth.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
final class ChangePasswordUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    void execute(final ChangePasswordCommand command) {
        log.debug("Attempting to change password for user ID: {}", command.getUserId());

        // Find user by ID
        final User user = userRepository.findById(command.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(command.getOldPassword(), user.getPassword())) {
            log.warn("Failed password change attempt for user ID: {} - incorrect old password", command.getUserId());
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Check if new password is the same as old password
        if (passwordEncoder.matches(command.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }

        // Encode and save new password
        final String encodedPassword = passwordEncoder.encode(command.getNewPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // Revoke all refresh tokens for security (force re-login on all devices)
        refreshTokenRepository.deleteByUser(user);

        log.info("Password changed successfully for user ID: {}", command.getUserId());
    }
}
