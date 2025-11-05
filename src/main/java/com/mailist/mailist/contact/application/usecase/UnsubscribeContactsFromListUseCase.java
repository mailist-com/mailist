package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.application.port.out.ContactListRepository;
import com.mailist.mailist.contact.application.port.out.ContactRepository;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.domain.service.ListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UnsubscribeContactsFromListUseCase {

    private final ContactListRepository contactListRepository;
    private final ContactRepository contactRepository;
    private final ListService listService;

    public SubscribeResult execute(SubscribeContactsCommand command) {
        log.info("Unsubscribing {} contacts from list ID: {}",
                command.getContactIds().size(), command.getListId());

        ContactList contactList = contactListRepository.findById(command.getListId())
                .orElseThrow(() -> new IllegalArgumentException("Contact list not found"));

        if (contactList.getIsDynamic()) {
            throw new IllegalStateException("Cannot manually unsubscribe contacts from a smart list");
        }

        int unsubscribed = 0;
        int failed = 0;

        for (Long contactId : command.getContactIds()) {
            try {
                Contact contact = contactRepository.findById(contactId)
                        .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + contactId));

                listService.removeContactFromList(contactList, contact);
                unsubscribed++;
            } catch (Exception e) {
                log.error("Failed to unsubscribe contact {} from list: {}", contactId, e.getMessage());
                failed++;
            }
        }

        contactListRepository.save(contactList);
        log.info("Unsubscription complete: {} unsubscribed, {} failed", unsubscribed, failed);

        return SubscribeResult.builder()
                .subscribed(unsubscribed)
                .failed(failed)
                .build();
    }
}
