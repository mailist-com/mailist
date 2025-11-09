package com.mailist.mailist.automation.domain.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.mailist.mailist.automation.domain.strategy.AbstractStepParsingStrategy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strategy for group-related steps (ADD_TO_GROUP, REMOVE_FROM_GROUP, SUBSCRIBER_JOINS_GROUP)
 */
@Component
public class GroupStepStrategy extends AbstractStepParsingStrategy {

    private static final List<String> SUPPORTED_TYPES = Arrays.asList(
        "ADD_TO_GROUP", "REMOVE_FROM_GROUP", "SUBSCRIBER_JOINS_GROUP"
    );

    public GroupStepStrategy() {
        super("GROUP_OPERATION");
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

        String groupName = getStringValue(nodeData, "groupName");
        if (groupName != null) {
            settings.put("groupName", groupName);
        }

        String name = getStringValue(nodeData, "name");
        if (name != null) {
            settings.put("name", name);
        }

        return settings;
    }
}
