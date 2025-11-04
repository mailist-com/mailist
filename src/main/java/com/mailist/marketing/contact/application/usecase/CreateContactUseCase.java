package com.mailist.marketing.contact.application.usecase;

import com.mailist.marketing.contact.domain.aggregate.Contact;
import com.mailist.marketing.contact.application.port.out.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateContactUseCase {
    
    private final ContactRepository contactRepository;
    
    public Contact execute(CreateContactCommand command) {
        if (contactRepository.existsByEmail(command.getEmail())) {
            throw new IllegalArgumentException("Contact with this email already exists");
        }
        
        Contact contact = Contact.builder()
                .firstName(command.getFirstName())
                .lastName(command.getLastName())
                .email(command.getEmail())
                .phone(command.getPhone())
                .leadScore(0)
                .build();
        
        return contactRepository.save(contact);
    }
}