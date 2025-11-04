package com.mailist.marketing.automation.application.usecase;

import com.mailist.marketing.automation.application.port.out.AutomationRuleRepository;
import com.mailist.marketing.automation.domain.aggregate.AutomationRule;
import com.mailist.marketing.automation.domain.service.AutomationEngine;
import com.mailist.marketing.contact.application.port.out.ContactRepository;
import com.mailist.marketing.contact.domain.aggregate.Contact;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecuteAutomationRuleUseCase {
    
    private final AutomationRuleRepository automationRuleRepository;
    private final AutomationEngine automationEngine;
    private final ContactRepository contactRepository;
    
    public void execute(ExecuteAutomationRuleCommand command) {
        log.info("Executing automation rule ID: {} for contact ID: {}", 
                command.getAutomationRuleId(), command.getContactId());
        
        AutomationRule automationRule = automationRuleRepository.findById(command.getAutomationRuleId())
                .orElseThrow(() -> new IllegalArgumentException("Automation rule not found with ID: " + command.getAutomationRuleId()));
        
        if (!automationRule.getIsActive()) {
            log.warn("Automation rule ID: {} is not active, skipping execution", command.getAutomationRuleId());
            return;
        }
        
        // Get the contact
        Contact contact = contactRepository.findById(command.getContactId())
                .orElseThrow(() -> new IllegalArgumentException("Contact not found with ID: " + command.getContactId()));
        
        // Create execution context
        Map<String, Object> context = new HashMap<>();
        context.put("manualExecution", true);
        context.put("executedAt", java.time.LocalDateTime.now());
        
        automationEngine.executeRule(automationRule, contact, context);
        
        log.info("Successfully executed automation rule ID: {} for contact ID: {}", 
                command.getAutomationRuleId(), command.getContactId());
    }
}