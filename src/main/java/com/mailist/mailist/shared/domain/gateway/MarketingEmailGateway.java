package com.mailist.mailist.shared.domain.gateway;

import com.mailist.mailist.shared.domain.model.MarketingEmailMessage;

/**
 * Gateway for sending marketing emails (campaigns, automation).
 * These emails include tracking and can be sent via specialized providers like EmailLabs.
 */
public interface MarketingEmailGateway {
    /**
     * Send a marketing email with tracking capabilities.
     * @param emailMessage the marketing email to send
     */
    void sendEmail(MarketingEmailMessage emailMessage);

    /**
     * Check if the gateway is healthy and ready to send emails.
     * @return true if healthy, false otherwise
     */
    boolean isHealthy();
}
