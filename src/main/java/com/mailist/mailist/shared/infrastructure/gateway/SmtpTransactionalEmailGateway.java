package com.mailist.mailist.shared.infrastructure.gateway;

import com.mailist.mailist.shared.domain.gateway.TransactionalEmailGateway;
import com.mailist.mailist.shared.domain.model.TransactionalEmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * SMTP implementation for transactional emails.
 * Uses JavaMailSender for reliable delivery of critical system emails
 * like verification codes, password resets, and welcome messages.
 *
 * Configured to use SMTP providers (Gmail for dev, OVH for production).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SmtpTransactionalEmailGateway implements TransactionalEmailGateway {

    private final JavaMailSender mailSender;

    @Value("${email.transactional.from.address}")
    private String fromAddress;

    @Value("${email.transactional.from.name}")
    private String fromName;

    @Override
    public void sendEmail(TransactionalEmailMessage emailMessage) {
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

            // Add email type as custom header for categorization
            if (emailMessage.getType() != null) {
                mimeMessage.setHeader("X-Email-Type", "TRANSACTIONAL");
                mimeMessage.setHeader("X-Transactional-Type", emailMessage.getType().name());
            }

            // Send email
            mailSender.send(mimeMessage);
            log.info("Transactional email sent successfully - Type: {}, To: {}, Subject: {}",
                    emailMessage.getType(), emailMessage.getTo(), emailMessage.getSubject());

        } catch (MessagingException e) {
            log.error("Failed to send transactional email - Type: {}, To: {}",
                    emailMessage.getType(), emailMessage.getTo(), e);
            throw new RuntimeException("Failed to send transactional email", e);
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            // Try to create a test message to verify mail sender is configured
            mailSender.createMimeMessage();
            log.debug("Transactional email gateway health check passed");
            return true;
        } catch (Exception e) {
            log.error("Transactional email gateway health check failed", e);
            return false;
        }
    }
}
