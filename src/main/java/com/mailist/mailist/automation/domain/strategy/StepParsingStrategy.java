package com.mailist.mailist.automation.domain.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.domain.aggregate.AutomationStep;

import java.util.Map;

/**
 * Interface for parsing different types of automation step nodes from JSON
 */
public interface StepParsingStrategy {

    /**
     * Check if this strategy can handle the given node type
     */
    boolean canHandle(String nodeType);

    /**
     * Parse node data from JSON into settings map
     */
    Map<String, Object> parseSettings(JsonNode nodeData);

    /**
     * Build AutomationStep from JSON node
     */
    AutomationStep buildStep(
        AutomationRule automationRule,
        String stepId,
        String stepType,
        JsonNode nodeJson,
        Integer order
    );
}
