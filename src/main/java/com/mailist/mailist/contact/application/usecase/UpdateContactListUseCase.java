package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.application.usecase.command.UpdateContactListCommand;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.infrastructure.repository.ContactListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
final class UpdateContactListUseCase {

    private final ContactListRepository contactListRepository;

    ContactList execute(final UpdateContactListCommand command) {
        log.info("Updating contact list with ID: {}", command.getId());

        // Find existing list
        final ContactList contactList = contactListRepository.findById(command.getId())
                .orElseThrow(() -> new IllegalArgumentException("List not found with ID: " + command.getId()));

        // Check if new name conflicts with existing list (excluding current list)
        if (command.getName() != null && !command.getName().equals(contactList.getName())) {
            if (contactListRepository.existsByName(command.getName())) {
                throw new IllegalArgumentException("List with name '" + command.getName() + "' already exists");
            }
            contactList.setName(command.getName());
        }

        // Update fields if provided
        if (command.getDescription() != null) {
            contactList.setDescription(command.getDescription());
        }

        if (command.getIsSmartList() != null) {
            contactList.setIsDynamic(command.getIsSmartList());
        }

        if (command.getSegmentRule() != null) {
            contactList.setSegmentRule(command.getSegmentRule());
        }

        if (command.getTags() != null) {
            contactList.setTags(command.getTags());
        }

        contactListRepository.save(contactList);
        log.info("Contact list updated successfully with ID: {}", contactList.getId());

        return contactList;
    }
}
