package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.application.port.out.ContactRepository;
import com.mailist.mailist.contact.application.port.out.ContactListRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateContactUseCase {

    private final ContactRepository contactRepository;
    private final ContactListRepository contactListRepository;

    public Contact execute(CreateContactCommand command) {
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
            List<ContactList> contactLists = contactListRepository.findAllByIds(command.getListIds());

            // Add contact to lists (owning side of relationship)
            for (ContactList list : contactLists) {
                list.getContacts().add(contact);
            }
        }
        contactRepository.save(contact);

        return contact;
    }
}