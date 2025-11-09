package com.mailist.mailist.automation.domain.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.mailist.mailist.automation.domain.strategy.AbstractStepParsingStrategy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strategy for field update steps (UPDATE_FIELD, FIELD_UPDATED)
 */
@Component
public class UpdateFieldStepStrategy extends AbstractStepParsingStrategy {

    private static final List<String> SUPPORTED_TYPES = Arrays.asList(
        "UPDATE_FIELD", "FIELD_UPDATED"
    );

    public UpdateFieldStepStrategy() {
        super("UPDATE_FIELD");
    }

    @Override
    public boolean canHandle(String nodeType) {
        return SUPPORTED_TYPES.contains(nodeType);
    }

    @Override
    public Map<String, Object> parseSettings(JsonNode nodeData) {
        Map<String, Object> settings = new HashMap<>();

        if (nodeData == null) {
            return settings;
        }

        String fieldName = getStringValue(nodeData, "fieldName");
        if (fieldName != null) {
            settings.put("fieldName", fieldName);
        }

        String fieldValue = getStringValue(nodeData, "fieldValue");
        if (fieldValue != null) {
            settings.put("fieldValue", fieldValue);
        }

        String name = getStringValue(nodeData, "name");
        if (name != null) {
            settings.put("name", name);
        }

        return settings;
    }
}
