package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.application.usecase.command.SubscribeContactsCommand;
import com.mailist.mailist.contact.application.usecase.dto.SubscribeResult;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.domain.event.ContactListJoinedEvent;
import com.mailist.mailist.contact.domain.service.ListService;
import com.mailist.mailist.contact.infrastructure.repository.ContactListRepository;
import com.mailist.mailist.contact.infrastructure.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
final class SubscribeContactsToListUseCase {

    private final ContactListRepository contactListRepository;
    private final ContactRepository contactRepository;
    private final ListService listService;
    private final ApplicationEventPublisher eventPublisher;

    SubscribeResult execute(final SubscribeContactsCommand command) {
        log.info("Subscribing {} contacts to list ID: {}",
                command.getContactIds().size(), command.getListId());

        final ContactList contactList = contactListRepository.findById(command.getListId())
                .orElseThrow(() -> new IllegalArgumentException("Contact list not found"));

        if (contactList.getIsDynamic()) {
            throw new IllegalStateException("Cannot manually subscribe contacts to a smart list");
        }

        int subscribed = 0;
        int failed = 0;

        for (Long contactId : command.getContactIds()) {
            try {
                final Contact contact = contactRepository.findById(contactId)
                        .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + contactId));

                listService.addContactToList(contactList, contact);
                subscribed++;

                // Publikuj event o dołączeniu kontaktu do listy dla automatyzacji
                eventPublisher.publishEvent(new ContactListJoinedEvent(
                        contact.getId(),
                        contact.getEmail(),
                        contactList.getId(),
                        contactList.getName()
                ));
            } catch (Exception e) {
                log.error("Failed to subscribe contact {} to list: {}", contactId, e.getMessage());
                failed++;
            }
        }

        contactListRepository.save(contactList);
        log.info("Subscription complete: {} subscribed, {} failed", subscribed, failed);

        return SubscribeResult.builder()
                .subscribed(subscribed)
                .failed(failed)
                .build();
    }
}
