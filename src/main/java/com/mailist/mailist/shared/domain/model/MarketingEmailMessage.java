package com.mailist.mailist.shared.domain.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a marketing email message (campaigns, automation).
 * Marketing emails include tracking IDs and campaign information for analytics.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarketingEmailMessage {
    private String from;
    private String to;
    private String subject;
    private String htmlContent;
    private String textContent;
    private Map<String, String> headers;
    private LocalDateTime scheduledAt;

    /**
     * Campaign ID for tracking which campaign this email belongs to.
     */
    private String campaignId;

    /**
     * Unique tracking ID for this specific email message.
     */
    private String trackingId;

    /**
     * Contact ID for personalization and tracking.
     */
    private String contactId;
}
