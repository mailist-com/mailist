package com.mailist.mailist.automation.application.eventhandler.strategy;

import com.mailist.mailist.automation.domain.valueobject.TriggerType;
import com.mailist.mailist.automation.domain.event.EmailOpenedEvent;
import com.mailist.mailist.automation.domain.service.AutomationEngine;
import com.mailist.mailist.automation.application.port.out.AutomationRuleRepository;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.application.port.out.ContactRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EmailOpenedEventHandler extends AbstractEventHandler<EmailOpenedEvent> {
    
    public EmailOpenedEventHandler(AutomationRuleRepository automationRuleRepository,
                                  ContactRepository contactRepository,
                                  AutomationEngine automationEngine) {
        super(automationRuleRepository, contactRepository, automationEngine);
    }
    
    @Override
    public Class<EmailOpenedEvent> getSupportedEventType() {
        return EmailOpenedEvent.class;
    }
    
    @Override
    protected TriggerType getTriggerType() {
        return TriggerType.EMAIL_OPENED;
    }
    
    @Override
    protected Long getContactId(EmailOpenedEvent event) {
        return event.getContactId();
    }
    
    @Override
    protected Map<String, Object> createContext(EmailOpenedEvent event) {
        Map<String, Object> context = new HashMap<>();
        context.put("emailOpened", true);
        context.put("campaignId", event.getCampaignId());
        context.put("messageId", event.getMessageId());
        context.put("eventTime", event.getOccurredAt());
        context.put("contactEmail", event.getContactEmail());
        return context;
    }
    
    @Override
    protected void performContactUpdates(Contact contact, EmailOpenedEvent event) {
        super.performContactUpdates(contact, event);
        contact.incrementLeadScore(2); // Award points for opening email
    }
}