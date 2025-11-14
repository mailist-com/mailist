package com.mailist.mailist.automation.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.domain.aggregate.AutomationStep;
import com.mailist.mailist.automation.domain.strategy.StepParsingStrategy;
import com.mailist.mailist.automation.domain.strategy.StepParsingStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Service for parsing flow JSON into automation steps
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlowJsonParserService {

    private final StepParsingStrategyFactory strategyFactory;
    private final ObjectMapper objectMapper;

    /**
     * Parse flow JSON string into a list of AutomationStep entities
     */
    public List<AutomationStep> parseFlowJson(String flowJson, AutomationRule automationRule) {
        List<AutomationStep> steps = new ArrayList<>();

        if (flowJson == null || flowJson.trim().isEmpty()) {
            log.debug("Flow JSON is null or empty, returning empty steps list");
            return steps;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(flowJson);
            JsonNode nodesNode = rootNode.get("nodes");

            if (nodesNode == null || !nodesNode.isObject()) {
                log.warn("Flow JSON does not contain 'nodes' object");
                return steps;
            }

            // Iterate over all nodes in the flow
            int order = 0;
            Iterator<Map.Entry<String, JsonNode>> nodeIterator = nodesNode.fields();
            while (nodeIterator.hasNext()) {
                Map.Entry<String, JsonNode> nodeEntry = nodeIterator.next();
                String nodeId = nodeEntry.getKey();
                JsonNode nodeJson = nodeEntry.getValue();

                try {
                    AutomationStep step = parseNode(nodeId, nodeJson, automationRule, order++);
                    if (step != null) {
                        steps.add(step);
                    }
                } catch (Exception e) {
                    log.error("Error parsing node {}: {}", nodeId, e.getMessage(), e);
                    // Continue parsing other nodes even if one fails
                }
            }

            log.info("Successfully parsed {} steps from flow JSON for automation rule {}",
                steps.size(), automationRule.getId());

        } catch (Exception e) {
            log.error("Error parsing flow JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse flow JSON", e);
        }

        return steps;
    }

    /**
     * Parse a single node from the flow JSON
     */
    private AutomationStep parseNode(String nodeId, JsonNode nodeJson, AutomationRule automationRule, int order) {
        // Get node type
        if (!nodeJson.has("type")) {
            log.warn("Node {} does not have a type field", nodeId);
            return null;
        }

        String nodeType = nodeJson.get("type").asText();

        // IMPORTANT: Skip trigger nodes - they are NOT execution steps!
        // Triggers define WHEN automation starts (stored in AutomationRule.triggerType)
        // Steps define WHAT to do (stored in AutomationStep)
        if ("trigger".equalsIgnoreCase(nodeType)) {
            log.debug("Skipping trigger node {} - triggers are not execution steps", nodeId);
            return null;
        }

        // Get appropriate strategy for this node type
        StepParsingStrategy strategy = strategyFactory.getStrategy(nodeType);

        log.debug("Parsing node {} of type {} using strategy {}",
            nodeId, nodeType, strategy.getClass().getSimpleName());

        // Use strategy to build the step
        return strategy.buildStep(automationRule, nodeId, nodeType, nodeJson, order);
    }
}
