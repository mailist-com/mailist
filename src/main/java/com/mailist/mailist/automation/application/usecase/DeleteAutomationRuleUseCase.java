package com.mailist.mailist.automation.application.usecase;

import com.mailist.mailist.automation.application.usecase.command.DeleteAutomationRuleCommand;
import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.infrastructure.repository.AutomationRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteAutomationRuleUseCase {
    
    private final AutomationRuleRepository automationRuleRepository;
    
    @Transactional
    public void execute(DeleteAutomationRuleCommand command) {
        log.info("Deleting automation rule with ID: {}", command.getId());
        
        // Verify rule exists
        AutomationRule automationRule = automationRuleRepository.findById(command.getId())
                .orElseThrow(() -> new IllegalArgumentException("Automation rule not found with ID: " + command.getId()));
        
        // Delete the rule
        automationRuleRepository.deleteById(command.getId());
        
        log.info("Successfully deleted automation rule with ID: {}", command.getId());
    }
}