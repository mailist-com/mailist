package com.mailist.mailist.automation.application.usecase;

import com.mailist.mailist.automation.application.usecase.command.UpdateAutomationRuleCommand;
import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.infrastructure.repository.AutomationRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateAutomationRuleUseCase {
    
    private final AutomationRuleRepository automationRuleRepository;
    
    @Transactional
    public AutomationRule execute(UpdateAutomationRuleCommand command) {
        log.info("Updating automation rule with ID: {}", command.getId());
        
        AutomationRule automationRule = automationRuleRepository.findById(command.getId())
                .orElseThrow(() -> new IllegalArgumentException("Automation rule not found with ID: " + command.getId()));
        
        // Update fields if provided
        if (command.getName() != null) {
            automationRule.setName(command.getName());
        }
        if (command.getDescription() != null) {
            automationRule.setDescription(command.getDescription());
        }
        if (command.getIsActive() != null) {
            automationRule.setIsActive(command.getIsActive());
        }
        if (command.getConditions() != null) {
            automationRule.setConditions(command.getConditions());
        }
        if (command.getActions() != null) {
            automationRule.setActions(command.getActions());
        }
        
        AutomationRule savedRule = automationRuleRepository.save(automationRule);
        log.info("Successfully updated automation rule with ID: {}", savedRule.getId());
        
        return savedRule;
    }
}