package com.mailist.mailist.automation.application.eventhandler.strategy;

import com.mailist.mailist.automation.domain.valueobject.TriggerType;
import com.mailist.mailist.automation.domain.service.AutomationEngine;
import com.mailist.mailist.automation.infrastructure.repository.AutomationRuleRepository;
import com.mailist.mailist.contact.domain.event.ContactTagAddedEvent;
import com.mailist.mailist.contact.infrastructure.repository.ContactRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ContactTagAddedEventHandler extends AbstractEventHandler<ContactTagAddedEvent> {
    
    public ContactTagAddedEventHandler(AutomationRuleRepository automationRuleRepository,
                                       ContactRepository contactRepository,
                                       AutomationEngine automationEngine) {
        super(automationRuleRepository, contactRepository, automationEngine);
    }
    
    @Override
    public Class<ContactTagAddedEvent> getSupportedEventType() {
        return ContactTagAddedEvent.class;
    }
    
    @Override
    protected TriggerType getTriggerType() {
        return TriggerType.TAG_ADDED;
    }
    
    @Override
    protected Long getContactId(ContactTagAddedEvent event) {
        return event.getContactId();
    }
    
    @Override
    protected Map<String, Object> createContext(ContactTagAddedEvent event) {
        Map<String, Object> context = new HashMap<>();
        context.put("tagAdded", event.getTagName());
        context.put("eventTime", event.getOccurredAt());
        context.put("contactEmail", event.getContactEmail());
        return context;
    }
}