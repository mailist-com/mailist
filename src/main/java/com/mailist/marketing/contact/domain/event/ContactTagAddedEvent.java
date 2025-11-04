package com.mailist.marketing.contact.domain.event;

import com.mailist.marketing.shared.domain.event.DomainEvent;
import lombok.Getter;

@Getter
public class ContactTagAddedEvent extends DomainEvent {
    private final Long contactId;
    private final String tagName;
    private final String contactEmail;
    
    public ContactTagAddedEvent(Long contactId, String tagName, String contactEmail) {
        super();
        this.contactId = contactId;
        this.tagName = tagName;
        this.contactEmail = contactEmail;
    }
}