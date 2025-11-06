package com.mailist.mailist.apikey.interfaces.controller;

import com.mailist.mailist.apikey.application.usecase.*;
import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import com.mailist.mailist.apikey.domain.aggregate.ApiKeyActivity;
import com.mailist.mailist.apikey.interfaces.dto.*;
import com.mailist.mailist.shared.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for API Key management.
 */
@RestController
@RequestMapping("/api/v1/api-keys")
@RequiredArgsConstructor
@Tag(name = "API Keys", description = "API Key management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ApiKeyController {

    private final CreateApiKeyUseCase createApiKeyUseCase;
    private final GetApiKeysUseCase getApiKeysUseCase;
    private final RevokeApiKeyUseCase revokeApiKeyUseCase;
    private final GetApiKeyActivitiesUseCase getApiKeyActivitiesUseCase;
    private final GetApiKeyStatisticsUseCase getApiKeyStatisticsUseCase;

    @GetMapping
    @Operation(summary = "Get all API keys", description = "Retrieve all API keys for the current organization")
    public ResponseEntity<ApiResponse<List<ApiKeyDto>>> getAllApiKeys() {
        List<ApiKey> apiKeys = getApiKeysUseCase.execute();
        List<ApiKeyDto> dtos = apiKeys.stream()
                .map(ApiKeyDto::fromEntity)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get API key by ID", description = "Retrieve a specific API key by its ID")
    public ResponseEntity<ApiResponse<ApiKeyDto>> getApiKeyById(@PathVariable String id) {
        ApiKey apiKey = getApiKeysUseCase.getById(id);
        return ResponseEntity.ok(ApiResponse.success(ApiKeyDto.fromEntity(apiKey)));
    }

    @PostMapping
    @Operation(summary = "Create API key", description = "Create a new API key. The plain key is shown only once!")
    public ResponseEntity<ApiResponse<CreatedApiKeyResponse>> createApiKey(
            @Valid @RequestBody CreateApiKeyRequest request
    ) {
        CreateApiKeyUseCase.CreateApiKeyCommand command = new CreateApiKeyUseCase.CreateApiKeyCommand(
                request.name(),
                request.description(),
                request.permissions(),
                request.expiresAt()
        );

        CreateApiKeyUseCase.CreatedApiKey result = createApiKeyUseCase.execute(command);

        CreatedApiKeyResponse response = new CreatedApiKeyResponse(
                ApiKeyDto.fromEntity(result.apiKey()),
                result.plainKey(),
                "API key created successfully. Save it now, it won't be shown again!"
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "API key created successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete API key", description = "Permanently delete an API key and its activities")
    public ResponseEntity<ApiResponse<Void>> deleteApiKey(@PathVariable String id) {
        revokeApiKeyUseCase.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "API key deleted successfully"));
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "Revoke API key", description = "Revoke an API key without deleting it")
    public ResponseEntity<ApiResponse<Void>> revokeApiKey(@PathVariable String id) {
        revokeApiKeyUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.success(null, "API key revoked successfully"));
    }

    @GetMapping("/{id}/activities")
    @Operation(summary = "Get API key activities", description = "Retrieve activity logs for a specific API key")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApiKeyActivities(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int pageSize
    ) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<ApiKeyActivity> activities = getApiKeyActivitiesUseCase.execute(id, pageable);

        List<ApiKeyActivityDto> dtos = activities.getContent().stream()
                .map(ApiKeyActivityDto::fromEntity)
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("activities", dtos);
        response.put("pagination", Map.of(
                "page", activities.getNumber(),
                "pageSize", activities.getSize(),
                "total", activities.getTotalElements(),
                "totalPages", activities.getTotalPages(),
                "hasNextPage", activities.hasNext(),
                "hasPreviousPage", activities.hasPrevious()
        ));

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get API key statistics", description = "Get usage statistics for all API keys")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        GetApiKeyStatisticsUseCase.ApiKeyStatistics stats = getApiKeyStatisticsUseCase.execute();

        Map<String, Object> response = new HashMap<>();
        response.put("totalKeys", stats.totalKeys());
        response.put("activeKeys", stats.activeKeys());
        response.put("totalCalls", stats.totalCalls());
        response.put("topEndpoints", stats.topEndpoints());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/permissions")
    @Operation(summary = "Get available permissions", description = "Get list of available API permissions")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getAvailablePermissions() {
        List<Map<String, String>> permissions = List.of(
                Map.of("permission", "contacts.read", "description", "Read contacts"),
                Map.of("permission", "contacts.write", "description", "Create and update contacts"),
                Map.of("permission", "contacts.delete", "description", "Delete contacts"),
                Map.of("permission", "campaigns.read", "description", "Read campaigns"),
                Map.of("permission", "campaigns.write", "description", "Create and update campaigns"),
                Map.of("permission", "campaigns.send", "description", "Send campaigns"),
                Map.of("permission", "lists.read", "description", "Read lists"),
                Map.of("permission", "lists.write", "description", "Create and update lists"),
                Map.of("permission", "templates.read", "description", "Read templates"),
                Map.of("permission", "templates.write", "description", "Create and update templates"),
                Map.of("permission", "automations.read", "description", "Read automations"),
                Map.of("permission", "automations.write", "description", "Create and update automations"),
                Map.of("permission", "*", "description", "Full access (admin)")
        );

        return ResponseEntity.ok(ApiResponse.success(permissions));
    }
}
