package com.mailist.mailist.contact.domain.service;

import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.domain.event.ContactListJoinedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public void addContactToList(ContactList list, Contact contact) {
        list.addContact(contact);
        
        // Publish event for automation triggers
        ContactListJoinedEvent event = new ContactListJoinedEvent(
                contact.getId(),
                contact.getEmail(),
                list.getId(),
                list.getName()
        );
        eventPublisher.publishEvent(event);
    }
    
    public void removeContactFromList(ContactList list, Contact contact) {
        list.removeContact(contact);
    }
    
    public List<Contact> getActiveContacts(ContactList list) {
        return list.getContacts().stream()
                .filter(contact -> contact.getLastActivityAt() != null &&
                        contact.getLastActivityAt().isAfter(
                                java.time.LocalDateTime.now().minusDays(30)
                        ))
                .collect(Collectors.toList());
    }
    
    public List<Contact> segmentContactsByTag(ContactList list, String tagName) {
        return list.getContacts().stream()
                .filter(contact -> contact.hasTag(tagName))
                .collect(Collectors.toList());
    }
    
    public List<Contact> segmentContactsByLeadScore(ContactList list, int minScore, int maxScore) {
        return list.getContacts().stream()
                .filter(contact -> contact.getLeadScore() >= minScore && 
                                 contact.getLeadScore() <= maxScore)
                .collect(Collectors.toList());
    }
}