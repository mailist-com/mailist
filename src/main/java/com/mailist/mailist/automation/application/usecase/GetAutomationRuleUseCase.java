package com.mailist.mailist.automation.application.usecase;

import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.infrastructure.repository.AutomationRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetAutomationRuleUseCase {
    
    private final AutomationRuleRepository automationRuleRepository;
    
    public AutomationRule execute(GetAutomationRuleQuery query) {
        log.debug("Retrieving automation rule with ID: {}", query.getId());
        
        return automationRuleRepository.findById(query.getId())
                .orElseThrow(() -> new IllegalArgumentException("Automation rule not found with ID: " + query.getId()));
    }
}