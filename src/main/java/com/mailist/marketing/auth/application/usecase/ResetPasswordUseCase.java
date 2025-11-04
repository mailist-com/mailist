package com.mailist.marketing.auth.application.usecase;

import com.mailist.marketing.auth.application.port.out.RefreshTokenRepository;
import com.mailist.marketing.auth.application.port.out.UserRepository;
import com.mailist.marketing.auth.domain.aggregate.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResetPasswordUseCase {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    
    public void execute(ResetPasswordCommand command) {
        log.info("Password reset attempt for email: {}", command.getEmail());
        
        // Find user by reset token
        User user = userRepository.findByPasswordResetToken(command.getResetCode())
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
        String encodedPassword = passwordEncoder.encode(command.getNewPassword());
        user.resetPassword(encodedPassword);
        userRepository.save(user);
        
        // Revoke all existing refresh tokens for security
        refreshTokenRepository.deleteByUser(user);
        
        log.info("Password reset successfully for user: {}", command.getEmail());
    }
}