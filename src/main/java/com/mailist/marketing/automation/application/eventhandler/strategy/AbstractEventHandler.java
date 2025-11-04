package com.mailist.marketing.automation.application.eventhandler.strategy;

import com.mailist.marketing.automation.domain.aggregate.AutomationRule;
import com.mailist.marketing.automation.domain.valueobject.TriggerType;
import com.mailist.marketing.automation.domain.service.AutomationEngine;
import com.mailist.marketing.automation.application.port.out.AutomationRuleRepository;
import com.mailist.marketing.contact.domain.aggregate.Contact;
import com.mailist.marketing.contact.application.port.out.ContactRepository;
import com.mailist.marketing.shared.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractEventHandler<T extends DomainEvent> implements EventHandlerStrategy<T> {
    
    protected final AutomationRuleRepository automationRuleRepository;
    protected final ContactRepository contactRepository;
    protected final AutomationEngine automationEngine;
    
    protected abstract TriggerType getTriggerType();
    protected abstract Map<String, Object> createContext(T event);
    protected abstract Long getContactId(T event);
    
    @Override
    public void handle(T event) {
        log.info("Processing {} for contact {}", 
                event.getClass().getSimpleName(), getContactId(event));
        
        List<AutomationRule> rules = automationRuleRepository.findByTriggerType(getTriggerType());
        
        Contact contact = contactRepository.findById(getContactId(event))
                .orElse(null);
        
        if (contact == null) {
            log.warn("Contact {} not found for automation", getContactId(event));
            return;
        }
        
        // Perform any contact updates specific to the event
        performContactUpdates(contact, event);
        
        Map<String, Object> context = createContext(event);
        executeMatchingRules(rules, contact, context);
    }
    
    protected void performContactUpdates(Contact contact, T event) {
        // Default implementation - can be overridden by specific handlers
        contact.updateActivity();
    }
    
    private void executeMatchingRules(List<AutomationRule> rules, Contact contact, Map<String, Object> context) {
        for (AutomationRule rule : rules) {
            if (rule.getIsActive()) {
                try {
                    log.info("Executing automation rule '{}' for contact {}", 
                            rule.getName(), contact.getId());
                    
                    automationEngine.executeRule(rule, contact, context);
                    contactRepository.save(contact);
                    
                    log.info("Successfully executed automation rule '{}' for contact {}", 
                            rule.getName(), contact.getId());
                } catch (Exception e) {
                    log.error("Failed to execute automation rule '{}' for contact {}: {}", 
                            rule.getName(), contact.getId(), e.getMessage(), e);
                }
            }
        }
    }
}