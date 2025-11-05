package com.mailist.mailist.shared.infrastructure.gateway;

import com.mailist.mailist.shared.domain.gateway.EmailGateway;
import com.mailist.mailist.shared.domain.model.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * SMTP implementation of EmailGateway using JavaMailSender.
 * Supports both Gmail (development) and OVH (production) SMTP servers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "email.gateway.type", havingValue = "smtp", matchIfMissing = true)
public class SmtpEmailGateway implements EmailGateway {

    private final JavaMailSender mailSender;

    @Value("${email.from.address}")
    private String fromAddress;

    @Value("${email.from.name}")
    private String fromName;

    @Override
    public void sendEmail(EmailMessage emailMessage) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            // Set from address (use configured default if not specified in message)
            String from = emailMessage.getFrom() != null
                ? emailMessage.getFrom()
                : String.format("%s <%s>", fromName, fromAddress);
            helper.setFrom(from);

            // Set recipient
            helper.setTo(emailMessage.getTo());

            // Set subject
            helper.setSubject(emailMessage.getSubject());

            // Set content (prefer HTML, fallback to text)
            if (emailMessage.getHtmlContent() != null) {
                helper.setText(emailMessage.getHtmlContent(), true);
            } else if (emailMessage.getTextContent() != null) {
                helper.setText(emailMessage.getTextContent(), false);
            } else {
                throw new IllegalArgumentException("Email must have either HTML or text content");
            }

            // Add custom headers if present
            if (emailMessage.getHeaders() != null) {
                emailMessage.getHeaders().forEach((key, value) -> {
                    try {
                        mimeMessage.setHeader(key, value);
                    } catch (MessagingException e) {
                        log.warn("Failed to set header {}: {}", key, e.getMessage());
                    }
                });
            }

            // Send email
            mailSender.send(mimeMessage);
            log.info("Email sent successfully to: {} with subject: {}",
                    emailMessage.getTo(), emailMessage.getSubject());

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", emailMessage.getTo(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            // Try to create a test message to verify mail sender is configured
            mailSender.createMimeMessage();
            return true;
        } catch (Exception e) {
            log.error("Email gateway health check failed", e);
            return false;
        }
    }
}
