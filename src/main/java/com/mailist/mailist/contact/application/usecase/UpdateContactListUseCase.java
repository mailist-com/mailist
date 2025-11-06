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
public class UpdateContactListUseCase {

    private final ContactListRepository contactListRepository;

    public ContactList execute(UpdateContactListCommand command) {
        log.info("Updating contact list with ID: {}", command.getId());

        // Find existing list
        ContactList contactList = contactListRepository.findById(command.getId())
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

        ContactList updatedList = contactListRepository.save(contactList);
        log.info("Contact list updated successfully with ID: {}", updatedList.getId());

        return updatedList;
    }
}
