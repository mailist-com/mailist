package com.mailist.mailist.externalapi.controller;

import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.externalapi.dto.ExternalApiResponse;
import com.mailist.mailist.externalapi.dto.ExternalContactRequest;
import com.mailist.mailist.externalapi.dto.ExternalContactResponse;
import com.mailist.mailist.externalapi.service.ExternalApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

/**
 * External API controller for contact management.
 * All endpoints require API key authentication via X-API-Key header.
 */
@RestController
@RequestMapping("/api/v1/external/contacts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "External API - Contacts", description = "External API endpoints for managing contacts (requires API key)")
@SecurityRequirement(name = "apiKey")
public class ExternalContactController {

    private final ExternalApiService externalApiService;

    @PostMapping
    @Operation(
            summary = "Create a new contact",
            description = "Create a new contact and optionally add to a mailing list. Requires 'contacts.write' permission."
    )
    public ResponseEntity<ExternalApiResponse<ExternalContactResponse>> createContact(
            @Valid @RequestBody ExternalContactRequest request
    ) {
        try {
            Contact contact = externalApiService.createContact(request);
            ExternalContactResponse response = ExternalContactResponse.fromEntity(contact);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ExternalApiResponse.success(response, "Contact created successfully"));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ExternalApiResponse.error(e.getMessage(), "INVALID_REQUEST"));

        } catch (AccessDeniedException e) {
            log.warn("Access denied: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ExternalApiResponse.error(e.getMessage(), "PERMISSION_DENIED"));

        } catch (Exception e) {
            log.error("Error creating contact: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ExternalApiResponse.error("Internal server error", "INTERNAL_ERROR"));
        }
    }

    @PutMapping("/{email}")
    @Operation(
            summary = "Update a contact",
            description = "Update an existing contact by email. Requires 'contacts.write' permission."
    )
    public ResponseEntity<ExternalApiResponse<ExternalContactResponse>> updateContact(
            @PathVariable String email,
            @Valid @RequestBody ExternalContactRequest request
    ) {
        try {
            Contact contact = externalApiService.updateContact(email, request);
            ExternalContactResponse response = ExternalContactResponse.fromEntity(contact);

            return ResponseEntity.ok(ExternalApiResponse.success(response, "Contact updated successfully"));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ExternalApiResponse.error(e.getMessage(), "CONTACT_NOT_FOUND"));

        } catch (AccessDeniedException e) {
            log.warn("Access denied: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ExternalApiResponse.error(e.getMessage(), "PERMISSION_DENIED"));

        } catch (Exception e) {
            log.error("Error updating contact: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ExternalApiResponse.error("Internal server error", "INTERNAL_ERROR"));
        }
    }

    @GetMapping("/{email}")
    @Operation(
            summary = "Get a contact by email",
            description = "Retrieve contact information by email. Requires 'contacts.read' permission."
    )
    public ResponseEntity<ExternalApiResponse<ExternalContactResponse>> getContact(
            @PathVariable String email
    ) {
        try {
            Contact contact = externalApiService.getContactByEmail(email);
            ExternalContactResponse response = ExternalContactResponse.fromEntity(contact);

            return ResponseEntity.ok(ExternalApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.warn("Contact not found: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ExternalApiResponse.error(e.getMessage(), "CONTACT_NOT_FOUND"));

        } catch (AccessDeniedException e) {
            log.warn("Access denied: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ExternalApiResponse.error(e.getMessage(), "PERMISSION_DENIED"));

        } catch (Exception e) {
            log.error("Error getting contact: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ExternalApiResponse.error("Internal server error", "INTERNAL_ERROR"));
        }
    }

    @PostMapping("/{contactId}/add-to-list")
    @Operation(
            summary = "Add contact to list",
            description = "Add a contact to a mailing list by list ID or name. Requires 'contacts.write' permission."
    )
    public ResponseEntity<ExternalApiResponse<ExternalContactResponse>> addContactToList(
            @PathVariable Long contactId,
            @RequestParam(required = false) Long listId,
            @RequestParam(required = false) String listName
    ) {
        try {
            if (listId == null && (listName == null || listName.isBlank())) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ExternalApiResponse.error(
                                "Either listId or listName must be provided",
                                "INVALID_REQUEST"
                        ));
            }

            Contact contact = externalApiService.addContactToList(contactId, listId, listName);
            ExternalContactResponse response = ExternalContactResponse.fromEntity(contact);

            return ResponseEntity.ok(ExternalApiResponse.success(
                    response,
                    "Contact added to list successfully"
            ));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ExternalApiResponse.error(e.getMessage(), "NOT_FOUND"));

        } catch (AccessDeniedException e) {
            log.warn("Access denied: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ExternalApiResponse.error(e.getMessage(), "PERMISSION_DENIED"));

        } catch (Exception e) {
            log.error("Error adding contact to list: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ExternalApiResponse.error("Internal server error", "INTERNAL_ERROR"));
        }
    }
}
