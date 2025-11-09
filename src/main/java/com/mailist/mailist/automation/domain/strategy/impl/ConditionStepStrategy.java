package com.mailist.mailist.automation.domain.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.mailist.mailist.automation.domain.strategy.AbstractStepParsingStrategy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strategy for condition-related steps (CONDITION, IF_ELSE)
 */
@Component
public class ConditionStepStrategy extends AbstractStepParsingStrategy {

    private static final List<String> SUPPORTED_TYPES = Arrays.asList(
        "CONDITION", "IF_ELSE"
    );

    public ConditionStepStrategy() {
        super("CONDITION");
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

        String conditionField = getStringValue(nodeData, "conditionField");
        if (conditionField != null) {
            settings.put("conditionField", conditionField);
        }

        String conditionOperator = getStringValue(nodeData, "conditionOperator");
        if (conditionOperator != null) {
            settings.put("conditionOperator", conditionOperator);
        }

        String conditionValue = getStringValue(nodeData, "conditionValue");
        if (conditionValue != null) {
            settings.put("conditionValue", conditionValue);
        }

        String name = getStringValue(nodeData, "name");
        if (name != null) {
            settings.put("name", name);
        }

        return settings;
    }
}
