package com.mailist.mailist.contact.infrastructure.config;

import com.mailist.mailist.contact.application.port.out.ContactRepository;
import com.mailist.mailist.contact.application.usecase.AddTagToContactUseCase;
import com.mailist.mailist.contact.application.usecase.CreateContactUseCase;
import com.mailist.mailist.contact.application.usecase.*;
import com.mailist.mailist.contact.domain.service.ContactService;
import com.mailist.mailist.contact.domain.service.ListService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ContactConfig {
    
    @Bean
    public CreateContactUseCase createContactUseCase(ContactRepository contactRepository) {
        return new CreateContactUseCase(contactRepository);
    }
    
    @Bean
    public AddTagToContactUseCase addTagToContactUseCase(
            ContactRepository contactRepository,
            ContactService contactService) {
        return new AddTagToContactUseCase(contactRepository, contactService);
    }
    
    @Bean
    public ContactService contactService(ApplicationEventPublisher eventPublisher) {
        return new ContactService(eventPublisher);
    }
    
    @Bean
    public ListService listService(ApplicationEventPublisher eventPublisher) {
        return new ListService(eventPublisher);
    }
}