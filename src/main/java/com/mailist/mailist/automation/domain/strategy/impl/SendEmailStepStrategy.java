package com.mailist.mailist.automation.domain.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.mailist.mailist.automation.domain.strategy.AbstractStepParsingStrategy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SendEmailStepStrategy extends AbstractStepParsingStrategy {

    public SendEmailStepStrategy() {
        super("SEND_EMAIL");
    }

    @Override
    public Map<String, Object> parseSettings(JsonNode nodeData) {
        Map<String, Object> settings = new HashMap<>();

        if (nodeData == null) {
            return settings;
        }

        // Parse email-specific settings
        String emailSubject = getStringValue(nodeData, "emailSubject");
        if (emailSubject != null) {
            settings.put("emailSubject", emailSubject);
        }

        String emailContent = getStringValue(nodeData, "emailContent");
        if (emailContent != null) {
            settings.put("emailContent", emailContent);
        }

        String emailTemplate = getStringValue(nodeData, "emailTemplate");
        if (emailTemplate != null) {
            settings.put("emailTemplate", emailTemplate);
        }

        String name = getStringValue(nodeData, "name");
        if (name != null) {
            settings.put("name", name);
        }

        return settings;
    }
}
