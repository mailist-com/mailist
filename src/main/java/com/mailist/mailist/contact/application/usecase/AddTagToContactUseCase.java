package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.application.usecase.command.AddTagToContactCommand;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.domain.service.ContactService;
import com.mailist.mailist.contact.domain.valueobject.Tag;
import com.mailist.mailist.contact.infrastructure.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
final class AddTagToContactUseCase {

    private final ContactRepository contactRepository;
    private final ContactService contactService;

    Contact execute(final AddTagToContactCommand command) {
        log.info("Adding tag '{}' to contact ID: {}", command.getTagName(), command.getContactId());

        final Contact contact = contactRepository.findById(command.getContactId())
                .orElseThrow(() -> new IllegalArgumentException("Contact not found with ID: " + command.getContactId()));

        final Tag tag = Tag.builder()
                .name(command.getTagName())
                .color(command.getTagColor())
                .description(command.getTagDescription())
                .build();

        contactService.addTagToContact(contact, tag);

        contactRepository.save(contact);
        log.info("Successfully added tag '{}' to contact ID: {}", command.getTagName(), command.getContactId());

        return contact;
    }
}