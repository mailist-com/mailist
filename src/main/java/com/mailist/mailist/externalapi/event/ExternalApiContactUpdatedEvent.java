package com.mailist.mailist.externalapi.event;

import com.mailist.mailist.contact.domain.aggregate.Contact;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a contact is updated via external API.
 */
@Getter
public class ExternalApiContactUpdatedEvent extends ApplicationEvent {

    private final Contact contact;
    private final Long apiKeyId;

    public ExternalApiContactUpdatedEvent(Object source, Contact contact, Long apiKeyId) {
        super(source);
        this.contact = contact;
        this.apiKeyId = apiKeyId;
    }
}
