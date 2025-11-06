package com.mailist.mailist.shared.domain.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Represents a transactional email message (verification, password reset, welcome).
 * Transactional emails are sent immediately and don't include marketing tracking.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionalEmailMessage {
    private String from;
    private String to;
    private String subject;
    private String htmlContent;
    private String textContent;

    /**
     * Type of transactional email for categorization and logging.
     */
    private TransactionalEmailType type;

    public enum TransactionalEmailType {
        EMAIL_VERIFICATION,
        PASSWORD_RESET,
        WELCOME_EMAIL,
        ACCOUNT_NOTIFICATION
    }
}
