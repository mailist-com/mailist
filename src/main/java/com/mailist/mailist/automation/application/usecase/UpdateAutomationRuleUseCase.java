package com.mailist.mailist.automation.application.usecase;

import com.mailist.mailist.automation.application.usecase.command.UpdateAutomationRuleCommand;
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
@Slf4j
public class UpdateAutomationRuleUseCase {

    private final AutomationRuleRepository automationRuleRepository;
    private final AutomationStepRepository automationStepRepository;
    private final FlowJsonParserService flowJsonParserService;

    @Transactional
    public AutomationRule execute(UpdateAutomationRuleCommand command) {
        log.info("Updating automation rule with ID: {}", command.getId());

        AutomationRule automationRule = automationRuleRepository.findById(command.getId())
                .orElseThrow(() -> new IllegalArgumentException("Automation rule not found with ID: " + command.getId()));

        // Update fields if provided
        if (command.getName() != null) {
            automationRule.setName(command.getName());
        }
        if (command.getDescription() != null) {
            automationRule.setDescription(command.getDescription());
        }
        if (command.getIsActive() != null) {
            automationRule.setIsActive(command.getIsActive());
        }
        if (command.getFlowJson() != null) {
            automationRule.setFlowJson(command.getFlowJson());
        }

        AutomationRule savedRule = automationRuleRepository.save(automationRule);
        log.info("Successfully updated automation rule with ID: {}", savedRule.getId());

        // If flowJson was updated, re-parse and save automation steps
        if (command.getFlowJson() != null && !command.getFlowJson().trim().isEmpty()) {
            try {
                // Delete existing steps for this automation rule
                automationStepRepository.deleteByAutomationRuleId(savedRule.getId());
                log.info("Deleted existing automation steps for rule ID: {}", savedRule.getId());

                // Parse and save new steps
                List<AutomationStep> steps = flowJsonParserService.parseFlowJson(command.getFlowJson(), savedRule);
                if (!steps.isEmpty()) {
                    automationStepRepository.saveAll(steps);
                    log.info("Saved {} new automation steps for rule ID: {}", steps.size(), savedRule.getId());
                }
            } catch (Exception e) {
                log.error("Failed to parse and save automation steps for rule ID: {}", savedRule.getId(), e);
                // Don't fail the whole operation, just log the error
            }
        }

        return savedRule;
    }
}