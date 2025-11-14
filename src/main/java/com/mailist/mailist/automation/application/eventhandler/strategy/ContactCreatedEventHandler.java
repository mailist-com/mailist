package com.mailist.mailist.automation.application.eventhandler.strategy;

import com.mailist.mailist.automation.domain.valueobject.TriggerType;
import com.mailist.mailist.automation.application.usecase.AutomationExecutionService;
import com.mailist.mailist.automation.infrastructure.repository.AutomationRuleRepository;
import com.mailist.mailist.contact.domain.event.ContactCreatedEvent;
import com.mailist.mailist.contact.infrastructure.repository.ContactRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ContactCreatedEventHandler extends AbstractEventHandler<ContactCreatedEvent> {

    public ContactCreatedEventHandler(AutomationRuleRepository automationRuleRepository,
                                      ContactRepository contactRepository,
                                      AutomationExecutionService automationExecutionService) {
        super(automationRuleRepository, contactRepository, automationExecutionService);
    }

    @Override
    public Class<ContactCreatedEvent> getSupportedEventType() {
        return ContactCreatedEvent.class;
    }

    @Override
    protected TriggerType getTriggerType() {
        return TriggerType.CONTACT_CREATED;
    }

    @Override
    protected Long getContactId(ContactCreatedEvent event) {
        return event.getContactId();
    }

    @Override
    protected Map<String, Object> createContext(ContactCreatedEvent event) {
        Map<String, Object> context = new HashMap<>();
        context.put("contactEmail", event.getEmail());
        context.put("contactFirstName", event.getFirstName());
        context.put("contactLastName", event.getLastName());
        context.put("eventTime", event.getOccurredAt());
        context.put("trigger", "contact_created");
        return context;
    }
}
