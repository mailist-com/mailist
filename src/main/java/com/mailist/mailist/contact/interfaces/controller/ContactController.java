package com.mailist.mailist.contact.interfaces.controller;

import com.mailist.mailist.contact.application.usecase.ContactApplicationService;
import com.mailist.mailist.contact.application.usecase.command.CreateContactCommand;
import com.mailist.mailist.contact.application.usecase.command.UpdateContactCommand;
import com.mailist.mailist.contact.application.usecase.command.AddTagToContactCommand;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.infrastructure.repository.ContactRepository;
import com.mailist.mailist.contact.interfaces.dto.ContactDto;
import com.mailist.mailist.contact.interfaces.mapper.ContactMapper;
import com.mailist.mailist.shared.interfaces.dto.ApiResponse;
import com.mailist.mailist.shared.interfaces.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contact Management", description = "Contact CRUD operations and management")
class ContactController {


    private final ContactApplicationService contactApplicationService;
    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;

    @PostMapping
    @Operation(summary = "Create a new contact")
    ResponseEntity<ApiResponse<ContactDto.Response>> createContact(
            @Valid @RequestBody final ContactDto.CreateRequest request) {
        log.info("Creating new contact with email: {}", request.getEmail());
        log.info("Received tags in request: {}", request.getTags());

        final CreateContactCommand command = contactMapper.toCreateCommand(request);
        log.info("Tags in command after mapping: {}", command.getTags());

        final Contact contact = contactApplicationService.create(command);
        log.info("Tags in created contact: {}", contact.getTags());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(contactMapper.toResponse(contact), "Contact created successfully"));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get contact statistics")
    ResponseEntity<ApiResponse<java.util.Map<String, Long>>> getContactStatistics() {
        log.info("Getting contact statistics");

        long total = contactRepository.count();
        // TODO: Implement proper status-based counting when status field is added to Contact entity
        long active = total; // For now, assume all contacts are active
        long unsubscribed = 0;
        long bounced = 0;
        long tagged = 0;

        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("total", total);
        stats.put("active", active);
        stats.put("unsubscribed", unsubscribed);
        stats.put("bounced", bounced);
        stats.put("tagged", tagged);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping
    @Operation(summary = "List all contacts with pagination")
    ResponseEntity<ApiResponse<PagedResponse<ContactDto.Response>>> listContacts(final Pageable pageable) {
        log.info("Listing contacts - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        final Page<Contact> contactsPage = contactRepository.findAll(pageable);

        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(contactsPage, contactMapper::toResponse)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get contact by ID")
    ResponseEntity<ApiResponse<ContactDto.Response>> getContact(@PathVariable final long id) {
        log.info("Getting contact with ID: {}", id);

        final Optional<Contact> contact = contactRepository.findById(id);

        return contact.map(value -> ResponseEntity.ok(ApiResponse.success(contactMapper.toResponse(value))))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Contact not found")));

    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get contact by email")
    ResponseEntity<ContactDto.Response> getContactByEmail(@PathVariable final String email) {
        log.info("Getting contact with email: {}", email);

        final Optional<Contact> contact = contactRepository.findByEmail(email);

        return contact.map(value -> ResponseEntity.ok(contactMapper.toResponse(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @PutMapping("/{id}")
    @Operation(summary = "Update contact")
    ResponseEntity<ApiResponse<ContactDto.Response>> updateContact(
            @PathVariable final long id,
            @Valid @RequestBody final ContactDto.UpdateRequest request) {

        log.info("Updating contact with ID: {}", id);

        final UpdateContactCommand command = contactMapper.toUpdateCommand(id, request);
        final Contact contact = contactApplicationService.update(command);
        final ContactDto.Response response = contactMapper.toResponse(contact);

        return ResponseEntity.ok(ApiResponse.success(response, "Contact updated successfully"));
    }

    @PostMapping("/{id}/tags")
    @Operation(summary = "Add tag to contact")
    ResponseEntity<ContactDto.Response> addTagToContact(
            @PathVariable final long id,
            @Valid @RequestBody final ContactDto.AddTagRequest request) {

        log.info("Adding tag to contact ID: {}", id);

        final AddTagToContactCommand command = AddTagToContactCommand.builder()
                .contactId(id)
                .tagName(request.getTagName())
                .tagColor(request.getTagColor())
                .tagDescription(request.getTagDescription())
                .build();

        final Contact contact = contactApplicationService.addTagToContact(command);
        final ContactDto.Response response = contactMapper.toResponse(contact);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete contact")
    public ResponseEntity<ApiResponse<Void>> deleteContact(@PathVariable Long id) {

        log.info("Deleting contact with ID: {}", id);

        if (!contactRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Contact not found"));
        }

        contactRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Contact deleted successfully"));
    }
}