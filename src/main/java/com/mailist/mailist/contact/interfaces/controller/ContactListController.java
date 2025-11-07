package com.mailist.mailist.contact.interfaces.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailist.mailist.contact.application.usecase.*;
import com.mailist.mailist.contact.application.usecase.command.*;
import com.mailist.mailist.contact.application.usecase.dto.GlobalStatistics;
import com.mailist.mailist.contact.application.usecase.dto.SubscribeResult;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.infrastructure.repository.ContactListRepository;
import com.mailist.mailist.contact.interfaces.dto.ContactListDto;
import com.mailist.mailist.contact.interfaces.mapper.ContactListMapper;
import com.mailist.mailist.shared.infrastructure.exception.EntityNotFoundException;
import com.mailist.mailist.shared.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lists")
@Tag(name = "Contact Lists", description = "Contact list management endpoints")
class ContactListController {

    private final ContactListApplicationService contactListApplicationService;
    private final ContactListRepository contactListRepository;
    private final ContactListMapper contactListMapper;

    @GetMapping
    @Operation(summary = "Get all contact lists")
    ResponseEntity<ApiResponse<List<ContactListDto.Response>>> getAllLists() {
        log.info("Fetching all contact lists");

        final List<ContactList> lists = contactListRepository.findByIsActive(true);
        final List<ContactListDto.Response> response = lists.stream()
                .map(contactListMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<ContactListDto.Response>>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get global statistics for all contact lists")
    ResponseEntity<ApiResponse<ContactListDto.GlobalStatisticsResponse>> getGlobalStatistics() {
        log.info("Fetching global list statistics");

        final GlobalStatistics stats = contactListApplicationService.globalListStatistics();

        final ContactListDto.GlobalStatisticsResponse response = ContactListDto.GlobalStatisticsResponse.builder()
                .totalLists(stats.totalLists())
                .activeLists(stats.activeLists())
                .totalSubscribers(stats.totalSubscribers())
                .averageEngagement(stats.averageEngagement())
                .build();

        return ResponseEntity.ok(ApiResponse.<ContactListDto.GlobalStatisticsResponse>builder()
                .success(true)
                .data(response)
                .message("Statistics retrieved successfully")
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get contact list by ID")
    ResponseEntity<ApiResponse<ContactListDto.Response>> getListById(@PathVariable final long id) {
        log.info("Fetching contact list with ID: {}", id);

        final ContactList list = contactListRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contact list", id));

        final ContactListDto.Response response = contactListMapper.toResponse(list);

        return ResponseEntity.ok(ApiResponse.<ContactListDto.Response>builder()
                .success(true)
                .data(response)
                .build());
    }

    @PostMapping
    @Operation(summary = "Create a new contact list")
    ResponseEntity<ApiResponse<ContactListDto.Response>> createList(@Valid @RequestBody final ContactListDto.CreateRequest request) {
        log.info("Creating new contact list: {}", request.getName());

        final CreateContactListCommand command = contactListMapper.toCreateCommand(request);
        final ContactList createdList = contactListApplicationService.createContactList(command);

        final ContactListDto.Response response = contactListMapper.toResponse(createdList);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ContactListDto.Response>builder()
                        .success(true)
                        .data(response)
                        .message("List created successfully")
                        .build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a contact list")
    ResponseEntity<ApiResponse<ContactListDto.Response>> updateList(
            @PathVariable final long id,
            @Valid @RequestBody final ContactListDto.UpdateRequest request) {
        log.info("Updating contact list with ID: {}", id);

        final UpdateContactListCommand command = contactListMapper.toUpdateCommand(id, request);
        final ContactList updatedList = contactListApplicationService.updateContactList(command);

        final ContactListDto.Response response = contactListMapper.toResponse(updatedList);

        return ResponseEntity.ok(ApiResponse.<ContactListDto.Response>builder()
                .success(true)
                .data(response)
                .message("List updated successfully")
                .build());
    }

    @PostMapping("/{listId}/subscribe")
    @Operation(summary = "Subscribe contacts to a list")
    ResponseEntity<ApiResponse<ContactListDto.SubscribeResponse>> subscribeContacts(
            @PathVariable final long listId,
            @Valid @RequestBody final ContactListDto.SubscribeRequest request) {
        log.info("Subscribing {} contacts to list ID: {}", request.getContactIds().size(), listId);

        final SubscribeContactsCommand command = SubscribeContactsCommand.builder()
                .listId(listId)
                .contactIds(request.getContactIds())
                .build();

        final SubscribeResult result = contactListApplicationService.subscribeContactToList(command);

        final ContactListDto.SubscribeResponse response = ContactListDto.SubscribeResponse.builder()
                .subscribed(result.getSubscribed())
                .failed(result.getFailed())
                .build();

        return ResponseEntity.ok(ApiResponse.<ContactListDto.SubscribeResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @PostMapping("/{listId}/unsubscribe")
    @Operation(summary = "Unsubscribe contacts from a list")
    ResponseEntity<ApiResponse<ContactListDto.SubscribeResponse>> unsubscribeContacts(
            @PathVariable final long listId,
            @Valid @RequestBody final ContactListDto.SubscribeRequest request) {
        log.info("Unsubscribing {} contacts from list ID: {}", request.getContactIds().size(), listId);

        final SubscribeContactsCommand command = SubscribeContactsCommand.builder()
                .listId(listId)
                .contactIds(request.getContactIds())
                .build();

        final SubscribeResult result = contactListApplicationService.unsubscribeContactFromList(command);

        final ContactListDto.SubscribeResponse response = ContactListDto.SubscribeResponse.builder()
                .subscribed(result.getSubscribed())
                .failed(result.getFailed())
                .build();

        return ResponseEntity.ok(ApiResponse.<ContactListDto.SubscribeResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a contact list")
    ResponseEntity<ApiResponse<Void>> deleteList(@PathVariable final long id) {
        log.info("Deleting contact list with ID: {}", id);

        final ContactList list = contactListRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contact list", id));

        contactListRepository.delete(list);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("List deleted successfully")
                .build());
    }

    @GetMapping("/{listId}/statistics")
    @Operation(summary = "Get list statistics")
    ResponseEntity<ApiResponse<ContactListDto.StatisticsResponse>> getListStatistics(@PathVariable final long listId) {
        log.info("Fetching statistics for list ID: {}", listId);

        final ContactList list = contactListRepository.findById(listId)
                .orElseThrow(() -> new EntityNotFoundException("Contact list", listId));

        final ContactListDto.StatisticsResponse statistics = contactListMapper.toStatistics(list);

        return ResponseEntity.ok(ApiResponse.<ContactListDto.StatisticsResponse>builder()
                .success(true)
                .data(statistics)
                .build());
    }
}
