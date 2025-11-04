package com.mailist.marketing.contact.domain.service;

import com.mailist.marketing.contact.domain.aggregate.Contact;
import com.mailist.marketing.contact.domain.valueobject.Tag;
import com.mailist.marketing.contact.domain.event.ContactTagAddedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContactService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public void addTagToContact(Contact contact, Tag tag) {
        contact.addTag(tag);
        
        // Publish event for automation triggers
        ContactTagAddedEvent event = new ContactTagAddedEvent(
                contact.getId(), 
                tag.getName(), 
                contact.getEmail()
        );
        eventPublisher.publishEvent(event);
    }
    
    public void removeTagFromContact(Contact contact, Tag tag) {
        contact.removeTag(tag);
    }
    
    public void updateLeadScore(Contact contact, int points) {
        contact.incrementLeadScore(points);
    }
    
    public boolean isActive(Contact contact) {
        return contact.getLastActivityAt() != null &&
               contact.getLastActivityAt().isAfter(
                   java.time.LocalDateTime.now().minusDays(30)
               );
    }
    
    public String getContactSegment(Contact contact) {
        if (contact.getLeadScore() > 80) {
            return "Hot Lead";
        } else if (contact.getLeadScore() > 50) {
            return "Warm Lead";
        } else if (contact.getLeadScore() > 20) {
            return "Cold Lead";
        } else {
            return "Prospect";
        }
    }
}