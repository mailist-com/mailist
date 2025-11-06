package com.mailist.mailist.shared.domain.gateway;

import com.mailist.mailist.shared.domain.model.TransactionalEmailMessage;

/**
 * Gateway for sending transactional emails (verification, password reset, welcome emails).
 * These emails are critical system communications and should use reliable SMTP providers.
 */
public interface TransactionalEmailGateway {
    /**
     * Send a transactional email immediately.
     * @param emailMessage the transactional email to send
     */
    void sendEmail(TransactionalEmailMessage emailMessage);

    /**
     * Check if the gateway is healthy and ready to send emails.
     * @return true if healthy, false otherwise
     */
    boolean isHealthy();
}
