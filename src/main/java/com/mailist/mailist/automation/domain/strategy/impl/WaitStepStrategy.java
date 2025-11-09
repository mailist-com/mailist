package com.mailist.mailist.automation.domain.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.mailist.mailist.automation.domain.strategy.AbstractStepParsingStrategy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WaitStepStrategy extends AbstractStepParsingStrategy {

    public WaitStepStrategy() {
        super("WAIT");
    }

    @Override
    public Map<String, Object> parseSettings(JsonNode nodeData) {
        Map<String, Object> settings = new HashMap<>();

        if (nodeData == null) {
            return settings;
        }

        // Parse wait-specific settings
        Integer waitDuration = getIntValue(nodeData, "waitDuration");
        if (waitDuration != null) {
            settings.put("waitDuration", waitDuration);
        }

        String waitUnit = getStringValue(nodeData, "waitUnit");
        if (waitUnit != null) {
            settings.put("waitUnit", waitUnit);
        }

        String name = getStringValue(nodeData, "name");
        if (name != null) {
            settings.put("name", name);
        }

        return settings;
    }
}
