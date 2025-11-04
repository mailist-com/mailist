package com.mailist.marketing.contact.infrastructure.config;

import com.mailist.marketing.contact.application.port.out.ContactRepository;
import com.mailist.marketing.contact.application.port.out.ContactListRepository;
import com.mailist.marketing.contact.application.usecase.*;
import com.mailist.marketing.contact.domain.service.ContactService;
import com.mailist.marketing.contact.domain.service.ListService;
import com.mailist.marketing.contact.infrastructure.repository.ContactRepositoryImpl;
import com.mailist.marketing.contact.infrastructure.repository.ContactListRepositoryImpl;
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