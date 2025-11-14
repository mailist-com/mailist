package com.mailist.mailist.contact.domain.event;

import com.mailist.mailist.shared.domain.event.DomainEvent;
import lombok.Getter;

@Getter
public class ContactCreatedEvent extends DomainEvent {
    private final Long contactId;
    private final String email;
    private final String firstName;
    private final String lastName;

    public ContactCreatedEvent(Long contactId, String email, String firstName, String lastName) {
        super();
        this.contactId = contactId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
