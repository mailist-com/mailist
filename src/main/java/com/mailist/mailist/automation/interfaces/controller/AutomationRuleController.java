package com.mailist.mailist.automation.interfaces.controller;

import com.mailist.mailist.automation.application.usecase.*;
import com.mailist.mailist.automation.application.usecase.command.CreateAutomationRuleCommand;
import com.mailist.mailist.automation.application.usecase.command.DeleteAutomationRuleCommand;
import com.mailist.mailist.automation.application.usecase.command.ExecuteAutomationRuleCommand;
import com.mailist.mailist.automation.application.usecase.command.UpdateAutomationRuleCommand;
import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.infrastructure.repository.AutomationRuleRepository;
import com.mailist.mailist.automation.interfaces.dto.AutomationRuleDto;
import com.mailist.mailist.automation.interfaces.mapper.AutomationRuleMapper;
import com.mailist.mailist.shared.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/automation-rules")
@RequiredArgsConstructor
@Tag(name = "Automation Rules", description = "Marketing automation rule management")
@SecurityRequirement(name = "bearerAuth")
public class AutomationRuleController {

    private final CreateAutomationRuleUseCase createAutomationRuleUseCase;
    private final GetAutomationRuleUseCase getAutomationRuleUseCase;
    private final UpdateAutomationRuleUseCase updateAutomationRuleUseCase;
    private final DeleteAutomationRuleUseCase deleteAutomationRuleUseCase;
    private final ExecuteAutomationRuleUseCase executeAutomationRuleUseCase;
    private final ToggleAutomationStatusUseCase toggleAutomationStatusUseCase;
    private final DuplicateAutomationRuleUseCase duplicateAutomationRuleUseCase;
    private final GetAutomationStatisticsUseCase getAutomationStatisticsUseCase;
    private final AutomationRuleRepository automationRuleRepository;
    private final AutomationRuleMapper automationRuleMapper;
    
    @PostMapping
    @Operation(summary = "Create a new automation rule")
    public ResponseEntity<ApiResponse<AutomationRuleDto.Response>> createAutomationRule(
            @Valid @RequestBody AutomationRuleDto.CreateRequest request) {

        CreateAutomationRuleCommand command = automationRuleMapper.toCreateCommand(request);
        AutomationRule automationRule = createAutomationRuleUseCase.execute(command);
        AutomationRuleDto.Response response = automationRuleMapper.toResponse(automationRule);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Automation rule created successfully"));
    }
    
    @GetMapping
    @Operation(summary = "List all automation rules")
    public ResponseEntity<ApiResponse<List<AutomationRuleDto.Response>>> listAutomationRules() {

        Page<AutomationRule> automationRules = automationRuleRepository.findAll(Pageable.unpaged());
        List<AutomationRuleDto.Response> responses = automationRules.getContent().stream()
                .map(automationRuleMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get automation rule by ID")
    public ResponseEntity<ApiResponse<AutomationRuleDto.Response>> getAutomationRule(
            @PathVariable Long id) {

        GetAutomationRuleQuery query = new GetAutomationRuleQuery(id);
        AutomationRule automationRule = getAutomationRuleUseCase.execute(query);
        AutomationRuleDto.Response response = automationRuleMapper.toResponse(automationRule);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update automation rule")
    public ResponseEntity<ApiResponse<AutomationRuleDto.Response>> updateAutomationRule(
            @PathVariable Long id,
            @Valid @RequestBody AutomationRuleDto.UpdateRequest request) {

        UpdateAutomationRuleCommand command = automationRuleMapper.toUpdateCommand(id, request);
        AutomationRule automationRule = updateAutomationRuleUseCase.execute(command);
        AutomationRuleDto.Response response = automationRuleMapper.toResponse(automationRule);

        return ResponseEntity.ok(ApiResponse.success(response, "Automation rule updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete automation rule")
    public ResponseEntity<ApiResponse<Void>> deleteAutomationRule(@PathVariable Long id) {
        DeleteAutomationRuleCommand command = new DeleteAutomationRuleCommand(id);
        deleteAutomationRuleUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.success(null, "Automation rule deleted successfully"));
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "Manually execute automation rule")
    public ResponseEntity<ApiResponse<Void>> executeAutomationRule(
            @PathVariable Long id,
            @RequestParam Long contactId) {

        ExecuteAutomationRuleCommand command = new ExecuteAutomationRuleCommand(id, contactId);
        executeAutomationRuleUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.success(null, "Automation rule executed successfully"));
    }

    @PostMapping("/{id}/toggle-status")
    @Operation(summary = "Toggle automation rule status", description = "Toggle status between active and inactive")
    public ResponseEntity<ApiResponse<AutomationRuleDto.Response>> toggleAutomationStatus(@PathVariable Long id) {
        AutomationRule automationRule = toggleAutomationStatusUseCase.execute(id);
        AutomationRuleDto.Response response = automationRuleMapper.toResponse(automationRule);
        return ResponseEntity.ok(ApiResponse.success(response, "Automation rule status toggled successfully"));
    }

    @PostMapping("/{id}/duplicate")
    @Operation(summary = "Duplicate automation rule", description = "Create a copy of an existing automation rule")
    public ResponseEntity<ApiResponse<AutomationRuleDto.Response>> duplicateAutomationRule(@PathVariable Long id) {
        AutomationRule duplicated = duplicateAutomationRuleUseCase.execute(id);
        AutomationRuleDto.Response response = automationRuleMapper.toResponse(duplicated);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Automation rule duplicated successfully"));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get automation rule statistics", description = "Get usage statistics for all automation rules")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        GetAutomationStatisticsUseCase.AutomationStatistics stats = getAutomationStatisticsUseCase.execute();

        Map<String, Object> response = new HashMap<>();
        response.put("total", stats.total());
        response.put("active", stats.active());
        response.put("inactive", stats.inactive());
        response.put("draft", stats.draft());
        response.put("paused", 0);  // Can be computed from inactive if needed

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}