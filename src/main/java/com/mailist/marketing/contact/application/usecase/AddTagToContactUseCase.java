package com.mailist.marketing.contact.application.usecase;

import com.mailist.marketing.contact.application.port.out.ContactRepository;
import com.mailist.marketing.contact.domain.aggregate.Contact;
import com.mailist.marketing.contact.domain.service.ContactService;
import com.mailist.marketing.contact.domain.valueobject.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddTagToContactUseCase {
    
    private final ContactRepository contactRepository;
    private final ContactService contactService;
    
    @Transactional
    public Contact execute(AddTagToContactCommand command) {
        log.info("Adding tag '{}' to contact ID: {}", command.getTagName(), command.getContactId());
        
        Contact contact = contactRepository.findById(command.getContactId())
                .orElseThrow(() -> new IllegalArgumentException("Contact not found with ID: " + command.getContactId()));
        
        Tag tag = Tag.builder()
                .name(command.getTagName())
                .color(command.getTagColor())
                .description(command.getTagDescription())
                .build();
        
        contactService.addTagToContact(contact, tag);
        
        Contact savedContact = contactRepository.save(contact);
        log.info("Successfully added tag '{}' to contact ID: {}", command.getTagName(), command.getContactId());
        
        return savedContact;
    }
}