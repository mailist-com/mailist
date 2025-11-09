package com.mailist.mailist.automation.application.usecase;

import com.mailist.mailist.automation.application.usecase.command.CreateAutomationRuleCommand;
import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.domain.aggregate.AutomationStep;
import com.mailist.mailist.automation.domain.service.FlowJsonParserService;
import com.mailist.mailist.automation.infrastructure.repository.AutomationRuleRepository;
import com.mailist.mailist.automation.infrastructure.repository.AutomationStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CreateAutomationRuleUseCase {

    private final AutomationRuleRepository automationRuleRepository;
    private final AutomationStepRepository automationStepRepository;
    private final FlowJsonParserService flowJsonParserService;

    public AutomationRule execute(CreateAutomationRuleCommand command) {
        // Build automation rule entity
        AutomationRule rule = AutomationRule.builder()
                .name(command.getName())
                .description(command.getDescription())
                .triggerType(command.getTriggerType())
                .isActive(true)
                .flowJson(command.getFlowJson())
                .build();

        // Save automation rule first
        AutomationRule savedRule = automationRuleRepository.save(rule);
        log.info("Created automation rule with ID: {}", savedRule.getId());

        // Parse and save automation steps from flowJson
        if (command.getFlowJson() != null && !command.getFlowJson().trim().isEmpty()) {
            try {
                List<AutomationStep> steps = flowJsonParserService.parseFlowJson(command.getFlowJson(), savedRule);
                if (!steps.isEmpty()) {
                    automationStepRepository.saveAll(steps);
                    log.info("Saved {} automation steps for rule ID: {}", steps.size(), savedRule.getId());
                }
            } catch (Exception e) {
                log.error("Failed to parse and save automation steps for rule ID: {}", savedRule.getId(), e);
                // Don't fail the whole operation, just log the error
            }
        }

        return savedRule;
    }
}