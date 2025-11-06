package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.application.port.out.ContactRepository;
import com.mailist.mailist.contact.application.port.out.ContactListRepository;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.shared.infrastructure.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UpdateContactUseCase {

    private final ContactRepository contactRepository;
    private final ContactListRepository contactListRepository;

    public Contact execute(UpdateContactCommand command) {
        log.info("Updating contact with ID: {}", command.getId());

        // Find existing contact
        Contact contact = contactRepository.findById(command.getId())
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
            Set<ContactList> currentLists = new HashSet<>(contact.getContactLists());
            for (ContactList list : currentLists) {
                list.removeContact(contact);
            }

            // Add new lists
            for (Long listId : command.getListIds()) {
                ContactList contactList = contactListRepository.findById(listId)
                        .orElseThrow(() -> new IllegalArgumentException("Contact list not found with id: " + listId));
                contactList.addContact(contact);
            }
        }

        Contact updatedContact = contactRepository.save(contact);
        log.info("Contact updated successfully with ID: {}", updatedContact.getId());

        return updatedContact;
    }
}
