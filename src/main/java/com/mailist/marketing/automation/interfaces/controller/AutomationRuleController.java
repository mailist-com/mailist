package com.mailist.marketing.automation.interfaces.controller;

import com.mailist.marketing.automation.application.usecase.*;
import com.mailist.marketing.automation.domain.aggregate.AutomationRule;
import com.mailist.marketing.automation.interfaces.dto.AutomationRuleDto;
import com.mailist.marketing.automation.interfaces.mapper.AutomationRuleMapper;
import com.mailist.marketing.automation.application.port.out.AutomationRuleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/automation-rules")
@RequiredArgsConstructor
@Tag(name = "Automation Rules", description = "Marketing automation rule management")
public class AutomationRuleController {
    
    private final CreateAutomationRuleUseCase createAutomationRuleUseCase;
    private final GetAutomationRuleUseCase getAutomationRuleUseCase;
    private final UpdateAutomationRuleUseCase updateAutomationRuleUseCase;
    private final DeleteAutomationRuleUseCase deleteAutomationRuleUseCase;
    private final ExecuteAutomationRuleUseCase executeAutomationRuleUseCase;
    private final AutomationRuleRepository automationRuleRepository;
    private final AutomationRuleMapper automationRuleMapper;
    
    @PostMapping
    @Operation(summary = "Create a new automation rule")
    public ResponseEntity<AutomationRuleDto.Response> createAutomationRule(
            @Valid @RequestBody AutomationRuleDto.CreateRequest request) {
        
        CreateAutomationRuleCommand command = automationRuleMapper.toCreateCommand(request);
        AutomationRule automationRule = createAutomationRuleUseCase.execute(command);
        AutomationRuleDto.Response response = automationRuleMapper.toResponse(automationRule);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @Operation(summary = "List all automation rules with pagination")
    public ResponseEntity<Page<AutomationRuleDto.Response>> listAutomationRules(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        
        Page<AutomationRule> automationRules = automationRuleRepository.findAll(pageable);
        Page<AutomationRuleDto.Response> response = automationRules.map(automationRuleMapper::toResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get automation rule by ID")
    public ResponseEntity<AutomationRuleDto.Response> getAutomationRule(
            @PathVariable Long id) {
        
        GetAutomationRuleQuery query = new GetAutomationRuleQuery(id);
        AutomationRule automationRule = getAutomationRuleUseCase.execute(query);
        AutomationRuleDto.Response response = automationRuleMapper.toResponse(automationRule);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update automation rule")
    public ResponseEntity<AutomationRuleDto.Response> updateAutomationRule(
            @PathVariable Long id,
            @Valid @RequestBody AutomationRuleDto.UpdateRequest request) {
        
        UpdateAutomationRuleCommand command = automationRuleMapper.toUpdateCommand(id, request);
        AutomationRule automationRule = updateAutomationRuleUseCase.execute(command);
        AutomationRuleDto.Response response = automationRuleMapper.toResponse(automationRule);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete automation rule")
    public ResponseEntity<Void> deleteAutomationRule(@PathVariable Long id) {
        DeleteAutomationRuleCommand command = new DeleteAutomationRuleCommand(id);
        deleteAutomationRuleUseCase.execute(command);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/execute")
    @Operation(summary = "Manually execute automation rule")
    public ResponseEntity<Void> executeAutomationRule(
            @PathVariable Long id,
            @RequestParam Long contactId) {
        
        ExecuteAutomationRuleCommand command = new ExecuteAutomationRuleCommand(id, contactId);
        executeAutomationRuleUseCase.execute(command);
        return ResponseEntity.ok().build();
    }
}