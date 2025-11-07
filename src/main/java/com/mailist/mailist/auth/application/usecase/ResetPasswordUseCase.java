package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.usecase.command.ResetPasswordCommand;
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
final class ResetPasswordUseCase {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    
    void execute(final ResetPasswordCommand command) {
        log.info("Password reset attempt for email: {}", command.getEmail());
        
        // Find user by reset token
        final User user = userRepository.findByPasswordResetToken(command.getResetCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset code"));
        
        // Check if email matches
        if (!user.getEmail().equals(command.getEmail())) {
            log.warn("Email mismatch for reset code: {} - Expected: {}, Got: {}", 
                    command.getResetCode(), user.getEmail(), command.getEmail());
            throw new IllegalArgumentException("Invalid reset code");
        }
        
        // Check if token is still valid
        if (!user.isPasswordResetTokenValid()) {
            log.warn("Expired reset token for user: {}", command.getEmail());
            throw new IllegalArgumentException("Reset code has expired. Please request a new one.");
        }
        
        // Reset password
        final String encodedPassword = passwordEncoder.encode(command.getNewPassword());
        user.resetPassword(encodedPassword);
        userRepository.save(user);
        
        // Revoke all existing refresh tokens for security
        refreshTokenRepository.deleteByUser(user);
        
        log.info("Password reset successfully for user: {}", command.getEmail());
    }
}