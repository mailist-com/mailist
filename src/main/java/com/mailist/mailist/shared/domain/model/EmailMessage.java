package com.mailist.mailist.shared.domain.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailMessage {
    private String from;
    private String to;
    private String subject;
    private String htmlContent;
    private String textContent;
    private Map<String, String> headers;
    private LocalDateTime scheduledAt;
    private String campaignId;
    private String trackingId;
}