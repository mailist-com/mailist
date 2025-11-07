package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.port.out.EmailService;
import com.mailist.mailist.auth.application.usecase.command.RequestPasswordResetCommand;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
final class RequestPasswordResetUseCase {
    
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    public void execute(final RequestPasswordResetCommand command) {
        log.info("Password reset requested for email: {}", command.getEmail());
        
        // Find user by email (don't reveal if user doesn't exist for security)
        var userOptional = userRepository.findByEmail(command.getEmail());
        
        if (userOptional.isPresent()) {
            final User user = userOptional.get();
            
            // Generate reset code
            final String resetCode = generateResetCode();
            user.setPasswordResetToken(resetCode);
            
            userRepository.save(user);
            
            // Send reset email
            emailService.sendPasswordResetEmail(
                user.getEmail(),
                resetCode,
                user.getFirstName()
            );
            
            log.info("Password reset email sent to: {}", command.getEmail());
        } else {
            log.info("Password reset requested for non-existent email: {}", command.getEmail());
            // Don't reveal that user doesn't exist
        }
        
        // Always return success for security (don't reveal if email exists)
    }
    
    private String generateResetCode() {
        // Generate 6-digit numeric code
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}