package com.mailist.mailist.auth.infrastructure.service;

import com.mailist.mailist.auth.application.port.out.EmailService;
import com.mailist.mailist.shared.domain.gateway.TransactionalEmailGateway;
import com.mailist.mailist.shared.domain.model.TransactionalEmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final TransactionalEmailGateway transactionalEmailGateway;
    
    @Override
    public void sendVerificationEmail(String email, String verificationCode, String firstName) {
        String subject = "Verify your email address";
        String content = buildVerificationEmailContent(firstName, verificationCode);

        TransactionalEmailMessage emailMessage = TransactionalEmailMessage.builder()
                .to(email)
                .subject(subject)
                .htmlContent(content)
                .type(TransactionalEmailMessage.TransactionalEmailType.EMAIL_VERIFICATION)
                .build();

        try {
            transactionalEmailGateway.sendEmail(emailMessage);
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

        TransactionalEmailMessage emailMessage = TransactionalEmailMessage.builder()
                .to(email)
                .subject(subject)
                .htmlContent(content)
                .type(TransactionalEmailMessage.TransactionalEmailType.PASSWORD_RESET)
                .build();

        try {
            transactionalEmailGateway.sendEmail(emailMessage);
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

        TransactionalEmailMessage emailMessage = TransactionalEmailMessage.builder()
                .to(email)
                .subject(subject)
                .htmlContent(content)
                .type(TransactionalEmailMessage.TransactionalEmailType.WELCOME_EMAIL)
                .build();

        try {
            transactionalEmailGateway.sendEmail(emailMessage);
            log.info("Welcome email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", email, e);
            // Don't throw exception for welcome email failure
        }
    }
    
    private String buildVerificationEmailContent(String firstName, String verificationCode) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 20px; text-align: center; color: white; }
                    .header h1 { margin: 0; font-size: 28px; font-weight: 600; }
                    .content { padding: 40px 30px; }
                    .greeting { font-size: 18px; font-weight: 500; margin-bottom: 20px; color: #333; }
                    .message { font-size: 16px; color: #555; margin-bottom: 30px; line-height: 1.8; }
                    .code-container { background-color: #f8f9fa; border: 2px dashed #667eea; border-radius: 8px; padding: 20px; text-align: center; margin: 30px 0; }
                    .code-label { font-size: 14px; color: #666; margin-bottom: 10px; text-transform: uppercase; letter-spacing: 1px; }
                    .code { font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 8px; font-family: 'Courier New', monospace; }
                    .expiry { font-size: 14px; color: #888; margin-top: 30px; padding: 15px; background-color: #fff3cd; border-left: 4px solid #ffc107; border-radius: 4px; }
                    .footer { padding: 30px; text-align: center; color: #888; font-size: 14px; background-color: #f8f9fa; border-top: 1px solid #e9ecef; }
                    .footer p { margin: 5px 0; }
                    .warning { font-size: 13px; color: #666; margin-top: 20px; font-style: italic; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✉️ Verify Your Email</h1>
                    </div>
                    <div class="content">
                        <div class="greeting">Hi %s! 👋</div>
                        <div class="message">
                            Thank you for signing up for <strong>Mailist</strong>! We're excited to have you on board.
                            <br><br>
                            To complete your registration and start building amazing email campaigns, please verify your email address using the code below:
                        </div>
                        <div class="code-container">
                            <div class="code-label">Verification Code</div>
                            <div class="code">%s</div>
                        </div>
                        <div class="expiry">
                            ⏱️ <strong>Important:</strong> This verification code will expire in 24 hours.
                        </div>
                        <div class="warning">
                            If you didn't create an account with Mailist, please ignore this email and no action is required.
                        </div>
                    </div>
                    <div class="footer">
                        <p><strong>The Mailist Team</strong></p>
                        <p>Building better email experiences, together.</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName, verificationCode);
    }
    
    private String buildPasswordResetEmailContent(String firstName, String resetCode) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); padding: 40px 20px; text-align: center; color: white; }
                    .header h1 { margin: 0; font-size: 28px; font-weight: 600; }
                    .content { padding: 40px 30px; }
                    .greeting { font-size: 18px; font-weight: 500; margin-bottom: 20px; color: #333; }
                    .message { font-size: 16px; color: #555; margin-bottom: 30px; line-height: 1.8; }
                    .code-container { background-color: #fff5f5; border: 2px dashed #f5576c; border-radius: 8px; padding: 20px; text-align: center; margin: 30px 0; }
                    .code-label { font-size: 14px; color: #666; margin-bottom: 10px; text-transform: uppercase; letter-spacing: 1px; }
                    .code { font-size: 32px; font-weight: bold; color: #f5576c; letter-spacing: 8px; font-family: 'Courier New', monospace; }
                    .expiry { font-size: 14px; color: #888; margin-top: 30px; padding: 15px; background-color: #fee; border-left: 4px solid #f5576c; border-radius: 4px; }
                    .footer { padding: 30px; text-align: center; color: #888; font-size: 14px; background-color: #f8f9fa; border-top: 1px solid #e9ecef; }
                    .footer p { margin: 5px 0; }
                    .warning { font-size: 13px; color: #666; margin-top: 20px; padding: 15px; background-color: #fff3cd; border-radius: 4px; border-left: 4px solid #ffc107; }
                    .security-note { font-size: 13px; color: #666; margin-top: 20px; font-style: italic; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Password Reset Request</h1>
                    </div>
                    <div class="content">
                        <div class="greeting">Hi %s,</div>
                        <div class="message">
                            We received a request to reset your password for your <strong>Mailist</strong> account.
                            <br><br>
                            Use the code below to reset your password:
                        </div>
                        <div class="code-container">
                            <div class="code-label">Reset Code</div>
                            <div class="code">%s</div>
                        </div>
                        <div class="expiry">
                            ⏱️ <strong>Urgent:</strong> This reset code will expire in 1 hour for security reasons.
                        </div>
                        <div class="warning">
                            🛡️ <strong>Security Notice:</strong> If you didn't request a password reset, please ignore this email. Your password will remain unchanged and your account is secure.
                        </div>
                        <div class="security-note">
                            For your security, never share this code with anyone. The Mailist team will never ask for your password or reset code.
                        </div>
                    </div>
                    <div class="footer">
                        <p><strong>The Mailist Team</strong></p>
                        <p>Your security is our priority.</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName, resetCode);
    }
    
    private String buildWelcomeEmailContent(String firstName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }
                    .container { max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #4facfe 0%%, #00f2fe 100%%); padding: 50px 20px; text-align: center; color: white; }
                    .header h1 { margin: 0; font-size: 32px; font-weight: 600; }
                    .header p { margin: 10px 0 0 0; font-size: 18px; opacity: 0.9; }
                    .content { padding: 40px 30px; }
                    .greeting { font-size: 20px; font-weight: 500; margin-bottom: 20px; color: #333; }
                    .message { font-size: 16px; color: #555; margin-bottom: 30px; line-height: 1.8; }
                    .features { margin: 30px 0; }
                    .feature { padding: 15px; margin: 15px 0; background-color: #f8f9fa; border-radius: 8px; border-left: 4px solid #4facfe; }
                    .feature-icon { font-size: 24px; margin-right: 10px; }
                    .feature-title { font-weight: 600; font-size: 16px; color: #333; margin-bottom: 5px; }
                    .feature-desc { font-size: 14px; color: #666; }
                    .cta-button { display: inline-block; background: linear-gradient(135deg, #4facfe 0%%, #00f2fe 100%%); color: white; padding: 15px 40px; text-decoration: none; border-radius: 6px; font-weight: 600; margin: 20px 0; text-align: center; }
                    .cta-button:hover { opacity: 0.9; }
                    .footer { padding: 30px; text-align: center; color: #888; font-size: 14px; background-color: #f8f9fa; border-top: 1px solid #e9ecef; }
                    .footer p { margin: 5px 0; }
                    .support { font-size: 13px; color: #666; margin-top: 20px; padding: 15px; background-color: #e7f5ff; border-radius: 4px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Welcome to Mailist!</h1>
                        <p>Your account is ready to go</p>
                    </div>
                    <div class="content">
                        <div class="greeting">Hi %s! 👋</div>
                        <div class="message">
                            Congratulations! Your email has been successfully verified and your <strong>Mailist</strong> account is now active.
                            <br><br>
                            You're all set to start building powerful email marketing campaigns and automations.
                        </div>

                        <div class="features">
                            <div class="feature">
                                <div class="feature-title"><span class="feature-icon">📋</span>Manage Contacts</div>
                                <div class="feature-desc">Create and organize contact lists with advanced segmentation</div>
                            </div>
                            <div class="feature">
                                <div class="feature-title"><span class="feature-icon">📧</span>Build Campaigns</div>
                                <div class="feature-desc">Design beautiful email campaigns with our intuitive editor</div>
                            </div>
                            <div class="feature">
                                <div class="feature-title"><span class="feature-icon">⚡</span>Automate Marketing</div>
                                <div class="feature-desc">Set up powerful automation rules to engage your audience</div>
                            </div>
                            <div class="feature">
                                <div class="feature-title"><span class="feature-icon">📊</span>Track Performance</div>
                                <div class="feature-desc">Analyze campaign results with detailed metrics and insights</div>
                            </div>
                        </div>

                        <div style="text-align: center;">
                            <a href="https://app.mailist.com" class="cta-button">Get Started Now →</a>
                        </div>

                        <div class="support">
                            💡 <strong>Need Help?</strong> Our support team is here for you. Feel free to reach out with any questions or check out our documentation to get started.
                        </div>
                    </div>
                    <div class="footer">
                        <p><strong>The Mailist Team</strong></p>
                        <p>Building better email experiences, together.</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName);
    }
}