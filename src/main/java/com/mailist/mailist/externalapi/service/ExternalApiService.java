package com.mailist.mailist.externalapi.service;

import com.mailist.mailist.apikey.infrastructure.security.ApiKeyAuthenticationToken;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.domain.valueobject.Tag;
import com.mailist.mailist.contact.infrastructure.repository.ContactListRepository;
import com.mailist.mailist.contact.infrastructure.repository.ContactRepository;
import com.mailist.mailist.externalapi.dto.ExternalContactRequest;
import com.mailist.mailist.externalapi.event.ExternalApiContactAddedEvent;
import com.mailist.mailist.externalapi.event.ExternalApiContactUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for handling external API operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiService {

    private final ContactRepository contactRepository;
    private final ContactListRepository contactListRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Create a new contact via external API.
     */
    @Transactional
    public Contact createContact(ExternalContactRequest request) {
        // Check permission
        checkPermission("contacts.write");

        // Check if contact already exists
        Optional<Contact> existingContact = contactRepository.findByEmail(request.getEmail());
        if (existingContact.isPresent()) {
            throw new IllegalArgumentException("Contact with this email already exists");
        }

        // Create contact
        Contact contact = Contact.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .leadScore(0)
                .build();

        // Add tags if provided
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            contact.setTags(request.getTags().stream()
                    .map(tagName -> Tag.builder().name(tagName).build())
                    .collect(Collectors.toSet()));
        }

        // Save contact
        contact = contactRepository.save(contact);

        // Add to list if specified
        if (request.getListId() != null || request.getListName() != null) {
            addContactToList(contact, request.getListId(), request.getListName());
        }

        // Publish event
        Long apiKeyId = getApiKeyId();
        eventPublisher.publishEvent(new ExternalApiContactAddedEvent(
                this,
                contact,
                apiKeyId,
                request.getListId() != null ? request.getListId() :
                    (request.getListName() != null ? findListIdByName(request.getListName()) : null)
        ));

        log.info("Contact created via external API: {} (API Key: {})", contact.getEmail(), apiKeyId);

        return contact;
    }

    /**
     * Update an existing contact via external API.
     */
    @Transactional
    public Contact updateContact(String email, ExternalContactRequest request) {
        // Check permission
        checkPermission("contacts.write");

        // Find existing contact
        Contact contact = contactRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + email));

        // Update fields
        if (request.getFirstName() != null) {
            contact.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            contact.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            contact.setPhone(request.getPhone());
        }

        // Update tags if provided
        if (request.getTags() != null) {
            contact.setTags(request.getTags().stream()
                    .map(tagName -> Tag.builder().name(tagName).build())
                    .collect(Collectors.toSet()));
        }

        // Save contact
        contact = contactRepository.save(contact);

        // Update list membership if specified
        if (request.getListId() != null || request.getListName() != null) {
            addContactToList(contact, request.getListId(), request.getListName());
        }

        // Publish event
        Long apiKeyId = getApiKeyId();
        eventPublisher.publishEvent(new ExternalApiContactUpdatedEvent(
                this,
                contact,
                apiKeyId
        ));

        log.info("Contact updated via external API: {} (API Key: {})", contact.getEmail(), apiKeyId);

        return contact;
    }

    /**
     * Add contact to a list by ID or name.
     */
    @Transactional
    public Contact addContactToList(Long contactId, Long listId, String listName) {
        // Check permission
        checkPermission("contacts.write");

        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found"));

        addContactToList(contact, listId, listName);

        return contact;
    }

    /**
     * Get contact by email.
     */
    @Transactional(readOnly = true)
    public Contact getContactByEmail(String email) {
        // Check permission
        checkPermission("contacts.read");

        return contactRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + email));
    }

    /**
     * Helper method to add contact to list.
     */
    private void addContactToList(Contact contact, Long listId, String listName) {
        ContactList list = null;

        if (listId != null) {
            list = contactListRepository.findById(listId)
                    .orElseThrow(() -> new IllegalArgumentException("List not found with ID: " + listId));
        } else if (listName != null && !listName.isBlank()) {
            list = contactListRepository.findByName(listName)
                    .orElseGet(() -> {
                        // Create new list if it doesn't exist
                        ContactList newList = ContactList.builder()
                                .name(listName)
                                .description("Created via external API")
                                .isDynamic(false)
                                .isActive(true)
                                .build();
                        return contactListRepository.save(newList);
                    });
        }

        if (list != null) {
            list.addContact(contact);
            contactListRepository.save(list);
            log.info("Contact {} added to list {}", contact.getEmail(), list.getName());
        }
    }

    /**
     * Find list ID by name.
     */
    private Long findListIdByName(String listName) {
        return contactListRepository.findByName(listName)
                .map(ContactList::getId)
                .orElse(null);
    }

    /**
     * Check if the current API key has the required permission.
     */
    private void checkPermission(String permission) {
        ApiKeyAuthenticationToken auth = (ApiKeyAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.hasPermission(permission)) {
            throw new AccessDeniedException("API key does not have permission: " + permission);
        }
    }

    /**
     * Get the current API key ID from security context.
     */
    private Long getApiKeyId() {
        ApiKeyAuthenticationToken auth = (ApiKeyAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getApiKeyId() : null;
    }
}
