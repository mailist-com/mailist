package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.port.out.EmailService;
import com.mailist.mailist.auth.application.port.out.UserRepository;
import com.mailist.mailist.auth.domain.aggregate.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VerifyEmailUseCase {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    public void execute(VerifyEmailCommand command) {
        log.info("Email verification attempt for: {}", command.getEmail());
        
        // Find user by verification token
        User user = userRepository.findByVerificationToken(command.getVerificationCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification code"));
        
        // Check if email matches
        if (!user.getEmail().equals(command.getEmail())) {
            log.warn("Email mismatch for verification code: {} - Expected: {}, Got: {}", 
                    command.getVerificationCode(), user.getEmail(), command.getEmail());
            throw new IllegalArgumentException("Invalid verification code");
        }
        
        // Check if token is still valid
        if (!user.isVerificationTokenValid()) {
            log.warn("Expired verification token for user: {}", command.getEmail());
            throw new IllegalArgumentException("Verification code has expired. Please request a new one.");
        }
        
        // Verify email
        user.verifyEmail();
        userRepository.save(user);
        
        // Send welcome email
        emailService.sendWelcomeEmail(
            user.getEmail(),
            user.getFirstName()
        );
        
        log.info("Email verified successfully for user: {}", command.getEmail());
    }
}