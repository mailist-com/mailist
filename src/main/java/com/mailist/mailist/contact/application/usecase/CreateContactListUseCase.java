package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.application.port.out.ContactListRepository;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreateContactListUseCase {

    private final ContactListRepository contactListRepository;

    public ContactList execute(CreateContactListCommand command) {
        log.info("Creating contact list: {}", command.getName());

        // Check if list with this name already exists
        if (contactListRepository.existsByName(command.getName())) {
            throw new IllegalArgumentException("List with name '" + command.getName() + "' already exists");
        }

        ContactList contactList = ContactList.builder()
                .name(command.getName())
                .description(command.getDescription())
                .isDynamic(command.getIsSmartList() != null ? command.getIsSmartList() : false)
                .isActive(true)
                .segmentRule(command.getSegmentRule())
                .build();

        ContactList savedList = contactListRepository.save(contactList);
        log.info("Contact list created successfully with ID: {}", savedList.getId());

        return savedList;
    }
}
