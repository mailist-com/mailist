package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.application.usecase.command.CreateContactListCommand;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.infrastructure.repository.ContactListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
final class CreateContactListUseCase {

    private final ContactListRepository contactListRepository;

    ContactList execute(final CreateContactListCommand command) {
        log.info("Creating contact list: {}", command.getName());

        if (contactListRepository.existsByName(command.getName())) {
            throw new IllegalArgumentException("List with name '" + command.getName() + "' already exists");
        }

        final ContactList contactList = ContactList.builder()
                .name(command.getName())
                .description(command.getDescription())
                .isDynamic(command.getIsSmartList() != null ? command.getIsSmartList() : false)
                .isActive(true)
                .segmentRule(command.getSegmentRule())
                .tags(command.getTags() != null ? command.getTags() : new java.util.HashSet<>())
                .build();

        contactListRepository.save(contactList);
        log.info("Contact list created successfully with ID: {}", contactList.getId());

        return contactList;
    }
}
