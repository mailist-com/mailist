package com.mailist.mailist.externalapi.event;

import com.mailist.mailist.contact.domain.aggregate.Contact;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a contact is added via external API.
 */
@Getter
public class ExternalApiContactAddedEvent extends ApplicationEvent {

    private final Contact contact;
    private final Long apiKeyId;
    private final Long listId;

    public ExternalApiContactAddedEvent(Object source, Contact contact, Long apiKeyId, Long listId) {
        super(source);
        this.contact = contact;
        this.apiKeyId = apiKeyId;
        this.listId = listId;
    }
}
