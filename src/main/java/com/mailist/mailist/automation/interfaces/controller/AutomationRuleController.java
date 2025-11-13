package com.mailist.mailist.automation.interfaces.controller;

import com.mailist.mailist.automation.application.usecase.*;
import com.mailist.mailist.automation.application.usecase.command.CreateAutomationRuleCommand;
import com.mailist.mailist.automation.application.usecase.command.DeleteAutomationRuleCommand;
import com.mailist.mailist.automation.application.usecase.command.ExecuteAutomationRuleCommand;
import com.mailist.mailist.automation.application.usecase.command.UpdateAutomationRuleCommand;
import com.mailist.mailist.automation.domain.aggregate.AutomationExecution;
import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.domain.aggregate.AutomationStepExecution;
import com.mailist.mailist.automation.infrastructure.repository.AutomationExecutionRepository;
import com.mailist.mailist.automation.infrastructure.repository.AutomationRuleRepository;
import com.mailist.mailist.automation.infrastructure.repository.AutomationStepExecutionRepository;
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
    private final AutomationExecutionRepository executionRepository;
    private final AutomationStepExecutionRepository stepExecutionRepository;
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

    @GetMapping("/{id}/executions")
    @Operation(summary = "Get execution history for automation rule",
               description = "Retrieve all executions for a specific automation rule")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> getExecutionHistory(
            @PathVariable Long id,
            Pageable pageable) {

        Page<AutomationExecution> executions = executionRepository.findByAutomationRuleId(id, pageable);

        Page<Map<String, Object>> response = executions.map(execution -> {
            Map<String, Object> executionData = new HashMap<>();
            executionData.put("id", execution.getId());
            executionData.put("contactId", execution.getContactId());
            executionData.put("contactEmail", execution.getContactEmail());
            executionData.put("status", execution.getStatus());
            executionData.put("startedAt", execution.getStartedAt());
            executionData.put("completedAt", execution.getCompletedAt());
            executionData.put("currentStepId", execution.getCurrentStepId());
            executionData.put("errorMessage", execution.getErrorMessage());
            return executionData;
        });

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{automationId}/executions/{executionId}")
    @Operation(summary = "Get execution details",
               description = "Get detailed execution information including all steps")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExecutionDetails(
            @PathVariable Long automationId,
            @PathVariable Long executionId) {

        AutomationExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("Execution not found"));

        List<AutomationStepExecution> steps = stepExecutionRepository.findByAutomationExecutionId(executionId);

        Map<String, Object> response = new HashMap<>();
        response.put("id", execution.getId());
        response.put("contactId", execution.getContactId());
        response.put("contactEmail", execution.getContactEmail());
        response.put("status", execution.getStatus());
        response.put("startedAt", execution.getStartedAt());
        response.put("completedAt", execution.getCompletedAt());
        response.put("currentStepId", execution.getCurrentStepId());
        response.put("errorMessage", execution.getErrorMessage());
        response.put("context", execution.getContext());

        List<Map<String, Object>> stepsData = steps.stream().map(step -> {
            Map<String, Object> stepData = new HashMap<>();
            stepData.put("id", step.getId());
            stepData.put("stepId", step.getStepId());
            stepData.put("stepType", step.getStepType());
            stepData.put("status", step.getStatus());
            stepData.put("startedAt", step.getStartedAt());
            stepData.put("completedAt", step.getCompletedAt());
            stepData.put("scheduledFor", step.getScheduledFor());
            stepData.put("errorMessage", step.getErrorMessage());
            stepData.put("retryCount", step.getRetryCount());
            stepData.put("inputData", step.getInputData());
            stepData.put("outputData", step.getOutputData());
            return stepData;
        }).toList();

        response.put("steps", stepsData);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/contacts/{contactId}/executions")
    @Operation(summary = "Get execution history for contact",
               description = "Retrieve all automation executions for a specific contact")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getContactExecutionHistory(
            @PathVariable Long contactId) {

        List<AutomationExecution> executions = executionRepository.findByContactIdOrderByStartedAtDesc(contactId);

        List<Map<String, Object>> response = executions.stream().map(execution -> {
            Map<String, Object> executionData = new HashMap<>();
            executionData.put("id", execution.getId());
            executionData.put("automationRuleId", execution.getAutomationRule().getId());
            executionData.put("automationRuleName", execution.getAutomationRule().getName());
            executionData.put("status", execution.getStatus());
            executionData.put("startedAt", execution.getStartedAt());
            executionData.put("completedAt", execution.getCompletedAt());
            executionData.put("errorMessage", execution.getErrorMessage());
            return executionData;
        }).toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}