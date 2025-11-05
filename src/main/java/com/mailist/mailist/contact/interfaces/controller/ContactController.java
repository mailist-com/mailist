package com.mailist.mailist.contact.interfaces.controller;

import com.mailist.mailist.contact.application.usecase.CreateContactUseCase;
import com.mailist.mailist.contact.application.usecase.CreateContactCommand;
import com.mailist.mailist.contact.application.usecase.AddTagToContactUseCase;
import com.mailist.mailist.contact.application.usecase.AddTagToContactCommand;
import com.mailist.mailist.contact.application.port.out.ContactRepository;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.interfaces.dto.ContactDto;
import com.mailist.mailist.contact.interfaces.mapper.ContactMapper;
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
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contact Management", description = "Contact CRUD operations and management")
public class ContactController {
    
    private final CreateContactUseCase createContactUseCase;
    private final AddTagToContactUseCase addTagToContactUseCase;
    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    
    @PostMapping
    @Operation(summary = "Create a new contact")
    public ResponseEntity<ContactDto.Response> createContact(
            @Valid @RequestBody ContactDto.CreateRequest request) {
        
        log.info("Creating new contact with email: {}", request.getEmail());
        
        CreateContactCommand command = contactMapper.toCreateCommand(request);
        Contact contact = createContactUseCase.execute(command);
        ContactDto.Response response = contactMapper.toResponse(contact);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "List all contacts with pagination")
    public ResponseEntity<Page<ContactDto.Response>> listContacts(Pageable pageable) {
        
        log.info("Listing contacts with pagination: {}", pageable);
        
        Page<Contact> contacts = contactRepository.findAll(pageable);
        Page<ContactDto.Response> response = contacts.map(contactMapper::toResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get contact by ID")
    public ResponseEntity<ContactDto.Response> getContact(@PathVariable Long id) {
        
        log.info("Getting contact with ID: {}", id);
        
        Optional<Contact> contact = contactRepository.findById(id);
        if (contact.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ContactDto.Response response = contactMapper.toResponse(contact.get());
        return ResponseEntity.ok(response);
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
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        
        log.info("Deleting contact with ID: {}", id);
        
        if (!contactRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        contactRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}