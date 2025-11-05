package com.mailist.mailist.auth.infrastructure.service;

import com.mailist.mailist.auth.application.port.out.EmailService;
import com.mailist.mailist.shared.domain.gateway.EmailGateway;
import com.mailist.mailist.shared.domain.model.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    private final EmailGateway emailGateway;
    
    @Override
    public void sendVerificationEmail(String email, String verificationCode, String firstName) {
        String subject = "Verify your email address";
        String content = buildVerificationEmailContent(firstName, verificationCode);
        
        EmailMessage emailMessage = EmailMessage.builder()
                .to(email)
                .subject(subject)
                .htmlContent(content)
                .build();
        
        try {
            emailGateway.sendEmail(emailMessage);
            log.info("Verification email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", email, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
    
    @Override
    public void sendPasswordResetEmail(String email, String resetCode, String firstName) {
        String subject = "Reset your password";
        String content = buildPasswordResetEmailContent(firstName, resetCode);
        
        EmailMessage emailMessage = EmailMessage.builder()
                .to(email)
                .subject(subject)
                .htmlContent(content)
                .build();
        
        try {
            emailGateway.sendEmail(emailMessage);
            log.info("Password reset email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
    
    @Override
    public void sendWelcomeEmail(String email, String firstName) {
        String subject = "Welcome to Mailist!";
        String content = buildWelcomeEmailContent(firstName);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(email)
                .subject(subject)
                .htmlContent(content)
                .build();

        try {
            emailGateway.sendEmail(emailMessage);
            log.info("Welcome email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", email, e);
            // Don't throw exception for welcome email failure
        }
    }
    
    private String buildVerificationEmailContent(String firstName, String verificationCode) {
        return String.format("""
            Hi %s,
            
            Thank you for signing up for Mailist! To complete your registration, please verify your email address using the code below:
            
            Verification Code: %s
            
            This code will expire in 24 hours.
            
            If you didn't create an account with Mailist, please ignore this email.
            
            Best regards,
            The Mailist Team
            """, firstName, verificationCode);
    }
    
    private String buildPasswordResetEmailContent(String firstName, String resetCode) {
        return String.format("""
            Hi %s,
            
            You have requested to reset your password for your Mailist account. Please use the code below to reset your password:
            
            Reset Code: %s
            
            This code will expire in 1 hour.
            
            If you didn't request a password reset, please ignore this email and your password will remain unchanged.
            
            Best regards,
            The Mailist Team
            """, firstName, resetCode);
    }
    
    private String buildWelcomeEmailContent(String firstName) {
        return String.format("""
            Hi %s,

            Welcome to Mailist! Your account has been successfully verified and is ready to use.

            You can now start:
            • Creating and managing contact lists
            • Building email campaigns
            • Setting up marketing automation
            • Analyzing your campaign performance

            Get started by logging into your account at https://app.mailist.com

            If you have any questions, feel free to reach out to our support team.

            Best regards,
            The Mailist Team
            """, firstName);
    }
}