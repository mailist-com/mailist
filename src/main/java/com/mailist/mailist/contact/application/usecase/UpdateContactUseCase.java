package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.application.usecase.command.UpdateContactCommand;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.infrastructure.repository.ContactListRepository;
import com.mailist.mailist.contact.infrastructure.repository.ContactRepository;
import com.mailist.mailist.shared.infrastructure.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
final class UpdateContactUseCase {

    private final ContactRepository contactRepository;
    private final ContactListRepository contactListRepository;

    Contact execute(final UpdateContactCommand command) {
        log.info("Updating contact with ID: {}", command.getId());

        // Find existing contact
        final Contact contact = contactRepository.findById(command.getId())
                .orElseThrow(() -> new EntityNotFoundException("Contact", command.getId()));

        // Update fields if provided
        if (command.getFirstName() != null) {
            contact.setFirstName(command.getFirstName());
        }

        if (command.getLastName() != null) {
            contact.setLastName(command.getLastName());
        }

        if (command.getEmail() != null) {
            // Check if email is unique (excluding current contact)
            contactRepository.findByEmail(command.getEmail())
                    .ifPresent(existingContact -> {
                        if (!existingContact.getId().equals(command.getId())) {
                            throw new IllegalArgumentException("Contact with email '" + command.getEmail() + "' already exists");
                        }
                    });
            contact.setEmail(command.getEmail());
        }

        if (command.getPhone() != null) {
            contact.setPhone(command.getPhone());
        }

        // Update list associations if provided
        if (command.getListIds() != null) {
            // Clear existing lists
            final Set<ContactList> currentLists = new HashSet<>(contact.getContactLists());
            for (ContactList list : currentLists) {
                list.removeContact(contact);
            }

            // Add new lists
            for (Long listId : command.getListIds()) {
                final ContactList contactList = contactListRepository.findById(listId)
                        .orElseThrow(() -> new IllegalArgumentException("Contact list not found with id: " + listId));
                contactList.addContact(contact);
            }
        }

        // Update tags if provided
        if (command.getTags() != null) {
            log.info("Updating tags for contact ID: {}", contact.getId());
            // Clear existing tags
            contact.getTags().clear();
            // Add new tags
            contact.getTags().addAll(command.getTags());
        }

        contactRepository.save(contact);
        log.info("Contact updated successfully with ID: {}", contact.getId());

        return contact;
    }
}
