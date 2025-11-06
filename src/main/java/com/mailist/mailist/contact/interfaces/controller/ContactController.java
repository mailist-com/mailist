package com.mailist.mailist.contact.interfaces.controller;

import com.mailist.mailist.contact.application.usecase.CreateContactUseCase;
import com.mailist.mailist.contact.application.usecase.CreateContactCommand;
import com.mailist.mailist.contact.application.usecase.UpdateContactUseCase;
import com.mailist.mailist.contact.application.usecase.UpdateContactCommand;
import com.mailist.mailist.contact.application.usecase.AddTagToContactUseCase;
import com.mailist.mailist.contact.application.usecase.AddTagToContactCommand;
import com.mailist.mailist.contact.application.port.out.ContactRepository;
import com.mailist.mailist.contact.domain.aggregate.Contact;
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
public class ContactController {

    private final CreateContactUseCase createContactUseCase;
    private final UpdateContactUseCase updateContactUseCase;
    private final AddTagToContactUseCase addTagToContactUseCase;
    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    
    @PostMapping
    @Operation(summary = "Create a new contact")
    public ResponseEntity<ApiResponse<ContactDto.Response>> createContact(
            @Valid @RequestBody ContactDto.CreateRequest request) {

        log.info("Creating new contact with email: {}", request.getEmail());

        CreateContactCommand command = contactMapper.toCreateCommand(request);
        Contact contact = createContactUseCase.execute(command);
        ContactDto.Response response = contactMapper.toResponse(contact);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Contact created successfully"));
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get contact statistics")
    public ResponseEntity<ApiResponse<java.util.Map<String, Long>>> getContactStatistics() {
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
    public ResponseEntity<ApiResponse<PagedResponse<ContactDto.Response>>> listContacts(Pageable pageable) {

        log.info("Listing contacts - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Contact> contactsPage = contactRepository.findAll(pageable);
        PagedResponse<ContactDto.Response> response = PagedResponse.of(contactsPage, contactMapper::toResponse);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get contact by ID")
    public ResponseEntity<ApiResponse<ContactDto.Response>> getContact(@PathVariable Long id) {

        log.info("Getting contact with ID: {}", id);

        Optional<Contact> contact = contactRepository.findById(id);
        if (contact.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Contact not found"));
        }

        ContactDto.Response response = contactMapper.toResponse(contact.get());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/email/{email}")
    @Operation(summary = "Get contact by email")
    public ResponseEntity<ContactDto.Response> getContactByEmail(@PathVariable String email) {

        log.info("Getting contact with email: {}", email);

        Optional<Contact> contact = contactRepository.findByEmail(email);
        if (contact.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ContactDto.Response response = contactMapper.toResponse(contact.get());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update contact")
    public ResponseEntity<ApiResponse<ContactDto.Response>> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactDto.UpdateRequest request) {

        log.info("Updating contact with ID: {}", id);

        UpdateContactCommand command = contactMapper.toUpdateCommand(id, request);
        Contact contact = updateContactUseCase.execute(command);
        ContactDto.Response response = contactMapper.toResponse(contact);

        return ResponseEntity.ok(ApiResponse.success(response, "Contact updated successfully"));
    }

    @PostMapping("/{id}/tags")
    @Operation(summary = "Add tag to contact")
    public ResponseEntity<ContactDto.Response> addTagToContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactDto.AddTagRequest request) {
        
        log.info("Adding tag to contact ID: {}", id);
        
        AddTagToContactCommand command = AddTagToContactCommand.builder()
                .contactId(id)
                .tagName(request.getTagName())
                .tagColor(request.getTagColor())
                .tagDescription(request.getTagDescription())
                .build();
        
        Contact contact = addTagToContactUseCase.execute(command);
        ContactDto.Response response = contactMapper.toResponse(contact);
        
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