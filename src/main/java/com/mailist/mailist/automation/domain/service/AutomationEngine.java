package com.mailist.mailist.automation.domain.service;

import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.automation.domain.valueobject.Condition;
import com.mailist.mailist.automation.domain.valueobject.Action;
import com.mailist.mailist.shared.domain.gateway.MarketingEmailGateway;
import com.mailist.mailist.shared.domain.model.MarketingEmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomationEngine {

    private final MarketingEmailGateway marketingEmailGateway;
    
    public void executeRule(AutomationRule rule, Contact contact, Map<String, Object> context) {
        if (!rule.getIsActive()) {
            return;
        }

        // TODO: Refactor to use AutomationStep entities instead of deprecated conditions/actions
        // The automation logic should now be executed based on the flow defined in automation_steps table
        // This requires parsing the flow and executing steps in the correct order

        log.warn("AutomationEngine.executeRule is using deprecated logic - needs refactoring to use AutomationStep entities");
    }
    
    private boolean evaluateConditions(List<Condition> conditions, Contact contact, Map<String, Object> context) {
        if (conditions.isEmpty()) {
            return true;
        }
        
        return conditions.stream().allMatch(condition -> evaluateCondition(condition, contact, context));
    }
    
    private boolean evaluateCondition(Condition condition, Contact contact, Map<String, Object> context) {
        switch (condition.getOperator()) {
            case HAS_TAG:
                return contact.hasTag(condition.getValue());
            case NOT_HAS_TAG:
                return !contact.hasTag(condition.getValue());
            case EQUALS:
                return evaluateFieldEquals(condition.getField(), condition.getValue(), contact, context);
            case NOT_EQUALS:
                return !evaluateFieldEquals(condition.getField(), condition.getValue(), contact, context);
            case CONTAINS:
                return evaluateFieldContains(condition.getField(), condition.getValue(), contact, context);
            case NOT_CONTAINS:
                return !evaluateFieldContains(condition.getField(), condition.getValue(), contact, context);
            case GREATER_THAN:
                return evaluateFieldGreaterThan(condition.getField(), condition.getValue(), contact, context);
            case LESS_THAN:
                return evaluateFieldLessThan(condition.getField(), condition.getValue(), contact, context);
            case EMAIL_OPENED:
                return context.containsKey("emailOpened") && 
                       Boolean.TRUE.equals(context.get("emailOpened"));
            case EMAIL_CLICKED:
                return context.containsKey("emailClicked") && 
                       Boolean.TRUE.equals(context.get("emailClicked"));
            default:
                return false;
        }
    }
    
    private void executeAction(Action action, Contact contact, Map<String, Object> context) {
        switch (action.getType()) {
            case ADD_TAG:
                // Implementation would be handled by ContactService
                break;
            case REMOVE_TAG:
                // Implementation would be handled by ContactService
                break;
            case SEND_EMAIL:
                sendAutomationEmail(action, contact, context);
                break;
            case MOVE_TO_LIST:
                // Implementation would be handled by ListService
                break;
            case REMOVE_FROM_LIST:
                // Implementation would be handled by ListService
                break;
            case UPDATE_LEAD_SCORE:
                try {
                    int points = Integer.parseInt(action.getValue());
                    contact.incrementLeadScore(points);
                } catch (NumberFormatException e) {
                    // Log error
                }
                break;
            case WAIT:
                // Implementation for delayed actions
                break;
            case WEBHOOK:
                // Implementation for webhook calls
                break;
            default:
                // Log unknown action type
                break;
        }
    }
    
    private boolean evaluateFieldEquals(String field, String value, Contact contact, Map<String, Object> context) {
        switch (field) {
            case "email":
                return contact.getEmail().equals(value);
            case "firstName":
                return contact.getFirstName().equals(value);
            case "lastName":
                return contact.getLastName().equals(value);
            default:
                return context.containsKey(field) && value.equals(String.valueOf(context.get(field)));
        }
    }
    
    private boolean evaluateFieldContains(String field, String value, Contact contact, Map<String, Object> context) {
        switch (field) {
            case "email":
                return contact.getEmail().contains(value);
            case "firstName":
                return contact.getFirstName().contains(value);
            case "lastName":
                return contact.getLastName().contains(value);
            default:
                return context.containsKey(field) && 
                       String.valueOf(context.get(field)).contains(value);
        }
    }
    
    private boolean evaluateFieldGreaterThan(String field, String value, Contact contact, Map<String, Object> context) {
        try {
            double numValue = Double.parseDouble(value);
            switch (field) {
                case "leadScore":
                    return contact.getLeadScore() > numValue;
                default:
                    if (context.containsKey(field)) {
                        double contextValue = Double.parseDouble(String.valueOf(context.get(field)));
                        return contextValue > numValue;
                    }
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean evaluateFieldLessThan(String field, String value, Contact contact, Map<String, Object> context) {
        try {
            double numValue = Double.parseDouble(value);
            switch (field) {
                case "leadScore":
                    return contact.getLeadScore() < numValue;
                default:
                    if (context.containsKey(field)) {
                        double contextValue = Double.parseDouble(String.valueOf(context.get(field)));
                        return contextValue < numValue;
                    }
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void sendAutomationEmail(Action action, Contact contact, Map<String, Object> context) {
        try {
            // The action.value should contain email template content or template ID
            // For now, we'll send a simple email with the value as content
            String campaignId = context.containsKey("campaignId")
                    ? String.valueOf(context.get("campaignId"))
                    : null;

            MarketingEmailMessage emailMessage = MarketingEmailMessage.builder()
                    .to(contact.getEmail())
                    .subject("Automated Email from Mailist")
                    .htmlContent(action.getValue())
                    .textContent(action.getValue())
                    .campaignId(campaignId)
                    .contactId(contact.getId().toString())
                    .trackingId(UUID.randomUUID().toString())
                    .build();

            marketingEmailGateway.sendEmail(emailMessage);
            log.info("Automation email sent successfully to contact: {}", contact.getEmail());
        } catch (Exception e) {
            log.error("Failed to send automation email to contact: {}", contact.getEmail(), e);
            // Don't rethrow - continue with other actions
        }
    }
}