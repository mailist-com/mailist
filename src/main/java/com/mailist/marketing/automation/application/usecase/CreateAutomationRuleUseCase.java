package com.mailist.marketing.automation.application.usecase;

import com.mailist.marketing.automation.domain.aggregate.AutomationRule;
import com.mailist.marketing.automation.application.port.out.AutomationRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateAutomationRuleUseCase {
    
    private final AutomationRuleRepository automationRuleRepository;
    
    public AutomationRule execute(CreateAutomationRuleCommand command) {
        AutomationRule rule = AutomationRule.builder()
                .name(command.getName())
                .description(command.getDescription())
                .triggerType(command.getTriggerType())
                .isActive(true)
                .build();
        
        if (command.getConditions() != null) {
            command.getConditions().forEach(rule::addCondition);
        }
        
        if (command.getActions() != null) {
            command.getActions().forEach(rule::addAction);
        }
        
        if (command.getElseActions() != null) {
            command.getElseActions().forEach(rule::addElseAction);
        }
        
        return automationRuleRepository.save(rule);
    }
}