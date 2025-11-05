package com.mailist.mailist.automation.domain.event;

import com.mailist.mailist.shared.domain.event.DomainEvent;
import lombok.Getter;

@Getter
public class EmailClickedEvent extends DomainEvent {
    private final Long contactId;
    private final String contactEmail;
    private final String campaignId;
    private final String messageId;
    private final String clickedUrl;
    
    public EmailClickedEvent(Long contactId, String contactEmail, String campaignId, String messageId, String clickedUrl) {
        super();
        this.contactId = contactId;
        this.contactEmail = contactEmail;
        this.campaignId = campaignId;
        this.messageId = messageId;
        this.clickedUrl = clickedUrl;
    }
}