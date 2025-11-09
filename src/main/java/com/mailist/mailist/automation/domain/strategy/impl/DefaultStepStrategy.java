package com.mailist.mailist.automation.domain.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.mailist.mailist.automation.domain.strategy.AbstractStepParsingStrategy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Default strategy for node types that don't have a specific strategy
 * This strategy will parse all fields from the data node as-is
 */
@Component
public class DefaultStepStrategy extends AbstractStepParsingStrategy {

    public DefaultStepStrategy() {
        super("DEFAULT");
    }

    @Override
    public boolean canHandle(String nodeType) {
        // This strategy can handle any node type (fallback)
        return true;
    }

    @Override
    public Map<String, Object> parseSettings(JsonNode nodeData) {
        Map<String, Object> settings = new HashMap<>();

        if (nodeData == null) {
            return settings;
        }

        // Parse all fields from nodeData
        Iterator<Map.Entry<String, JsonNode>> fields = nodeData.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();

            // Convert JsonNode to appropriate Java type
            if (fieldValue.isTextual()) {
                settings.put(fieldName, fieldValue.asText());
            } else if (fieldValue.isInt()) {
                settings.put(fieldName, fieldValue.asInt());
            } else if (fieldValue.isLong()) {
                settings.put(fieldName, fieldValue.asLong());
            } else if (fieldValue.isDouble() || fieldValue.isFloat()) {
                settings.put(fieldName, fieldValue.asDouble());
            } else if (fieldValue.isBoolean()) {
                settings.put(fieldName, fieldValue.asBoolean());
            } else if (fieldValue.isNull()) {
                settings.put(fieldName, null);
            } else {
                // For complex types (arrays, objects), store as string
                settings.put(fieldName, fieldValue.toString());
            }
        }

        return settings;
    }
}
