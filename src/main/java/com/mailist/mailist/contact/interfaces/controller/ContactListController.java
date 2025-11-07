package com.mailist.mailist.contact.interfaces.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailist.mailist.contact.application.port.out.ContactListRepository;
import com.mailist.mailist.contact.application.usecase.*;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.interfaces.dto.ContactListDto;
import com.mailist.mailist.contact.interfaces.mapper.ContactListMapper;
import com.mailist.mailist.shared.infrastructure.exception.EntityNotFoundException;
import com.mailist.mailist.shared.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/lists")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Contact Lists", description = "Contact list management endpoints")
public class ContactListController {

    private final CreateContactListUseCase createContactListUseCase;
    private final UpdateContactListUseCase updateContactListUseCase;
    private final SubscribeContactsToListUseCase subscribeContactsToListUseCase;
    private final UnsubscribeContactsFromListUseCase unsubscribeContactsFromListUseCase;
    private final ImportContactsUseCase importContactsUseCase;
    private final ExportContactsUseCase exportContactsUseCase;
    private final GetGlobalListStatisticsUseCase getGlobalListStatisticsUseCase;
    private final ContactListRepository contactListRepository;
    private final ContactListMapper contactListMapper;
    private final ObjectMapper objectMapper;

    @GetMapping
    @Operation(summary = "Get all contact lists")
    public ResponseEntity<ApiResponse<List<ContactListDto.Response>>> getAllLists() {
        log.info("Fetching all contact lists");

        List<ContactList> lists = contactListRepository.findByIsActive(true);
        List<ContactListDto.Response> response = lists.stream()
                .map(contactListMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<ContactListDto.Response>>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get global statistics for all contact lists")
    public ResponseEntity<ApiResponse<ContactListDto.GlobalStatisticsResponse>> getGlobalStatistics() {
        log.info("Fetching global list statistics");

        GetGlobalListStatisticsUseCase.GlobalStatistics stats = getGlobalListStatisticsUseCase.execute();

        ContactListDto.GlobalStatisticsResponse response = ContactListDto.GlobalStatisticsResponse.builder()
                .totalLists(stats.getTotalLists())
                .activeLists(stats.getActiveLists())
                .totalSubscribers(stats.getTotalSubscribers())
                .averageEngagement(stats.getAverageEngagement())
                .build();

        return ResponseEntity.ok(ApiResponse.<ContactListDto.GlobalStatisticsResponse>builder()
                .success(true)
                .data(response)
                .message("Statistics retrieved successfully")
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get contact list by ID")
    public ResponseEntity<ApiResponse<ContactListDto.Response>> getListById(@PathVariable Long id) {
        log.info("Fetching contact list with ID: {}", id);

        ContactList list = contactListRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contact list", id));

        ContactListDto.Response response = contactListMapper.toResponse(list);

        return ResponseEntity.ok(ApiResponse.<ContactListDto.Response>builder()
                .success(true)
                .data(response)
                .build());
    }

    @PostMapping
    @Operation(summary = "Create a new contact list")
    public ResponseEntity<ApiResponse<ContactListDto.Response>> createList(
            @Valid @RequestBody ContactListDto.CreateRequest request) {

        log.info("Creating new contact list: {}", request.getName());

        CreateContactListCommand command = contactListMapper.toCreateCommand(request);
        ContactList createdList = createContactListUseCase.execute(command);

        ContactListDto.Response response = contactListMapper.toResponse(createdList);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ContactListDto.Response>builder()
                        .success(true)
                        .data(response)
                        .message("List created successfully")
                        .build());
    }

    @PostMapping("/smart")
    @Operation(summary = "Create a smart list with conditions")
    public ResponseEntity<ApiResponse<ContactListDto.Response>> createSmartList(
            @Valid @RequestBody ContactListDto.CreateSmartListRequest request) {

        log.info("Creating smart list: {}", request.getName());

        CreateContactListCommand command = contactListMapper.toCreateSmartListCommand(request);
        ContactList createdList = createContactListUseCase.execute(command);

        ContactListDto.Response response = contactListMapper.toResponse(createdList);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ContactListDto.Response>builder()
                        .success(true)
                        .data(response)
                        .message("Smart list created successfully")
                        .build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a contact list")
    public ResponseEntity<ApiResponse<ContactListDto.Response>> updateList(
            @PathVariable Long id,
            @Valid @RequestBody ContactListDto.UpdateRequest request) {

        log.info("Updating contact list with ID: {}", id);

        UpdateContactListCommand command = contactListMapper.toUpdateCommand(id, request);
        ContactList updatedList = updateContactListUseCase.execute(command);

        ContactListDto.Response response = contactListMapper.toResponse(updatedList);

        return ResponseEntity.ok(ApiResponse.<ContactListDto.Response>builder()
                .success(true)
                .data(response)
                .message("List updated successfully")
                .build());
    }

    @PostMapping("/{listId}/subscribe")
    @Operation(summary = "Subscribe contacts to a list")
    public ResponseEntity<ApiResponse<ContactListDto.SubscribeResponse>> subscribeContacts(
            @PathVariable Long listId,
            @Valid @RequestBody ContactListDto.SubscribeRequest request) {

        log.info("Subscribing {} contacts to list ID: {}", request.getContactIds().size(), listId);

        SubscribeContactsCommand command = SubscribeContactsCommand.builder()
                .listId(listId)
                .contactIds(request.getContactIds())
                .build();

        SubscribeResult result = subscribeContactsToListUseCase.execute(command);

        ContactListDto.SubscribeResponse response = ContactListDto.SubscribeResponse.builder()
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
    public ResponseEntity<ApiResponse<ContactListDto.SubscribeResponse>> unsubscribeContacts(
            @PathVariable Long listId,
            @Valid @RequestBody ContactListDto.SubscribeRequest request) {

        log.info("Unsubscribing {} contacts from list ID: {}", request.getContactIds().size(), listId);

        SubscribeContactsCommand command = SubscribeContactsCommand.builder()
                .listId(listId)
                .contactIds(request.getContactIds())
                .build();

        SubscribeResult result = unsubscribeContactsFromListUseCase.execute(command);

        ContactListDto.SubscribeResponse response = ContactListDto.SubscribeResponse.builder()
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
    public ResponseEntity<ApiResponse<Void>> deleteList(@PathVariable Long id) {
        log.info("Deleting contact list with ID: {}", id);

        ContactList list = contactListRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contact list", id));

        contactListRepository.delete(list);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("List deleted successfully")
                .build());
    }

    @GetMapping("/{listId}/statistics")
    @Operation(summary = "Get list statistics")
    public ResponseEntity<ApiResponse<ContactListDto.StatisticsResponse>> getListStatistics(
            @PathVariable Long listId) {

        log.info("Fetching statistics for list ID: {}", listId);

        ContactList list = contactListRepository.findById(listId)
                .orElseThrow(() -> new EntityNotFoundException("Contact list", listId));

        ContactListDto.StatisticsResponse statistics = contactListMapper.toStatistics(list);

        return ResponseEntity.ok(ApiResponse.<ContactListDto.StatisticsResponse>builder()
                .success(true)
                .data(statistics)
                .build());
    }

    @PostMapping("/{listId}/import")
    @Operation(summary = "Import contacts from CSV file")
    public ResponseEntity<ApiResponse<ImportContactsResult>> importContacts(
            @PathVariable Long listId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mapping", required = false) String mappingJson,
            @RequestParam(value = "skipDuplicates", defaultValue = "true") Boolean skipDuplicates) {

        log.info("Importing contacts from CSV to list ID: {}", listId);

        try {
            // Parse mapping JSON if provided
            Map<String, String> mapping = null;
            if (mappingJson != null && !mappingJson.isEmpty()) {
                mapping = objectMapper.readValue(mappingJson, new TypeReference<Map<String, String>>() {});
            }

            ImportContactsCommand command = ImportContactsCommand.builder()
                    .listId(listId)
                    .file(file)
                    .mapping(mapping)
                    .skipDuplicates(skipDuplicates)
                    .build();

            ImportContactsResult result = importContactsUseCase.execute(command);

            return ResponseEntity.ok(ApiResponse.<ImportContactsResult>builder()
                    .success(true)
                    .data(result)
                    .message("Import completed successfully")
                    .build());

        } catch (Exception e) {
            log.error("Error importing contacts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<ImportContactsResult>builder()
                            .success(false)
                            .message("Import failed: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/{listId}/export")
    @Operation(summary = "Export contacts to CSV file")
    public ResponseEntity<String> exportContacts(
            @PathVariable Long listId,
            @RequestParam(value = "format", defaultValue = "csv") String format,
            @RequestParam(value = "fields", required = false) String fieldsParam) {

        log.info("Exporting contacts from list ID: {} in format: {}", listId, format);

        try {
            List<String> fields = null;
            if (fieldsParam != null && !fieldsParam.isEmpty()) {
                fields = Arrays.asList(fieldsParam.split(","));
            }

            ExportContactsCommand command = ExportContactsCommand.builder()
                    .listId(listId)
                    .format(format)
                    .fields(fields)
                    .build();

            String csvContent = exportContactsUseCase.execute(command);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("text", "csv"));
            headers.setContentDispositionFormData("attachment", "contacts-list-" + listId + ".csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvContent);

        } catch (Exception e) {
            log.error("Error exporting contacts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Export failed: " + e.getMessage());
        }
    }
}
