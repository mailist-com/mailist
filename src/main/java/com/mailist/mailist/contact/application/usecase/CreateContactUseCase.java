package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.application.usecase.command.CreateContactCommand;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.infrastructure.repository.ContactListRepository;
import com.mailist.mailist.contact.infrastructure.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
final class CreateContactUseCase {

    private final ContactRepository contactRepository;
    private final ContactListRepository contactListRepository;

    Contact execute(final CreateContactCommand command) {
        if (contactRepository.existsByEmail(command.getEmail())) {
            throw new IllegalArgumentException("Contact with this email already exists");
        }

        final Contact contact = Contact.builder()
                .firstName(command.getFirstName())
                .lastName(command.getLastName())
                .email(command.getEmail())
                .phone(command.getPhone())
                .leadScore(0)
                .build();

        if (CollectionUtils.isNotEmpty(command.getListIds())) {
            final List<ContactList> contactLists = contactListRepository.findAllByIds(command.getListIds());

            // Add contact to lists (owning side of relationship)
            for (ContactList list : contactLists) {
                list.getContacts().add(contact);
            }
        }
        contactRepository.save(contact);

        return contact;
    }
}