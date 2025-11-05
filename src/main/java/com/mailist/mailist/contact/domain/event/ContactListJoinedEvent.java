package com.mailist.mailist.contact.domain.event;

import com.mailist.mailist.shared.domain.event.DomainEvent;
import lombok.Getter;

@Getter
public class ContactListJoinedEvent extends DomainEvent {
    private final Long contactId;
    private final String contactEmail;
    private final Long listId;
    private final String listName;
    
    public ContactListJoinedEvent(Long contactId, String contactEmail, Long listId, String listName) {
        super();
        this.contactId = contactId;
        this.contactEmail = contactEmail;
        this.listId = listId;
        this.listName = listName;
    }
}