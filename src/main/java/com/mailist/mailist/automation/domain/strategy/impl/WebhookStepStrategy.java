package com.mailist.mailist.automation.domain.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.mailist.mailist.automation.domain.strategy.AbstractStepParsingStrategy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WebhookStepStrategy extends AbstractStepParsingStrategy {

    public WebhookStepStrategy() {
        super("SEND_WEBHOOK");
    }

    @Override
    public Map<String, Object> parseSettings(JsonNode nodeData) {
        Map<String, Object> settings = new HashMap<>();

        if (nodeData == null) {
            return settings;
        }

        String webhookUrl = getStringValue(nodeData, "webhookUrl");
        if (webhookUrl != null) {
            settings.put("webhookUrl", webhookUrl);
        }

        String name = getStringValue(nodeData, "name");
        if (name != null) {
            settings.put("name", name);
        }

        return settings;
    }
}
