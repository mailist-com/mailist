package com.mailist.mailist.automation.domain.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.domain.aggregate.AutomationStep;

import java.util.HashMap;
import java.util.Map;

/**
 * Base implementation for step parsing strategies with common logic
 */
public abstract class AbstractStepParsingStrategy implements StepParsingStrategy {

    protected final String supportedNodeType;

    protected AbstractStepParsingStrategy(String supportedNodeType) {
        this.supportedNodeType = supportedNodeType;
    }

    @Override
    public boolean canHandle(String nodeType) {
        return supportedNodeType.equals(nodeType);
    }

    @Override
    public AutomationStep buildStep(
        AutomationRule automationRule,
        String stepId,
        String stepType,
        JsonNode nodeJson,
        Integer order
    ) {
        // Parse basic node properties
        JsonNode positionNode = nodeJson.get("position");
        Double positionX = positionNode != null && positionNode.has("x") ? positionNode.get("x").asDouble() : 0.0;
        Double positionY = positionNode != null && positionNode.has("y") ? positionNode.get("y").asDouble() : 0.0;

        // Parse input connection
        String inputConnectionId = nodeJson.has("input") ? nodeJson.get("input").asText() : null;

        // Parse output connections
        Map<String, String> outputConnections = new HashMap<>();
        JsonNode outputsNode = nodeJson.get("outputs");
        if (outputsNode != null && outputsNode.isArray()) {
            for (JsonNode output : outputsNode) {
                String outputId = output.get("id").asText();
                String outputName = output.has("name") ? output.get("name").asText() : "Output";
                outputConnections.put(outputId, outputName);
            }
        }

        // Parse node data (title, description, etc.)
        JsonNode dataNode = nodeJson.get("data");
        String title = null;
        String description = null;
        if (dataNode != null) {
            title = dataNode.has("title") ? dataNode.get("title").asText() : null;
            description = dataNode.has("description") ? dataNode.get("description").asText() : null;
        }

        // Parse specific settings for this node type
        Map<String, Object> settings = parseSettings(dataNode);

        // Build the step
        return AutomationStep.builder()
            .automationRule(automationRule)
            .stepId(stepId)
            .stepType(stepType)
            .stepOrder(order)
            .positionX(positionX)
            .positionY(positionY)
            .inputConnectionId(inputConnectionId)
            .outputConnections(outputConnections)
            .settings(settings)
            .title(title)
            .description(description)
            .isExpanded(nodeJson.has("isExpanded") ? nodeJson.get("isExpanded").asBoolean() : false)
            .build();
    }

    /**
     * Helper method to safely get string value from JsonNode
     */
    protected String getStringValue(JsonNode node, String fieldName) {
        if (node != null && node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asText();
        }
        return null;
    }

    /**
     * Helper method to safely get int value from JsonNode
     */
    protected Integer getIntValue(JsonNode node, String fieldName) {
        if (node != null && node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asInt();
        }
        return null;
    }

    /**
     * Helper method to safely get double value from JsonNode
     */
    protected Double getDoubleValue(JsonNode node, String fieldName) {
        if (node != null && node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asDouble();
        }
        return null;
    }

    /**
     * Helper method to safely get boolean value from JsonNode
     */
    protected Boolean getBooleanValue(JsonNode node, String fieldName) {
        if (node != null && node.has(fieldName) && !node.get(fieldName).isNull()) {
            return node.get(fieldName).asBoolean();
        }
        return null;
    }
}
