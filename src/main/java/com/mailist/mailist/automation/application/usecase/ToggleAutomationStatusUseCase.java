package com.mailist.mailist.automation.application.usecase;

import com.mailist.mailist.automation.application.port.out.AutomationRuleRepository;
import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToggleAutomationStatusUseCase {

    private final AutomationRuleRepository repository;

    @Transactional
    public AutomationRule execute(Long automationRuleId) {
        long organizationId = TenantContext.getOrganizationId();

        AutomationRule automationRule = repository.findById(automationRuleId)
                .filter(rule -> rule.getTenantId().equals(organizationId))
                .orElseThrow(() -> new IllegalArgumentException("Automation rule not found: " + automationRuleId));

        if (automationRule.getIsActive()) {
            automationRule.deactivate();
        } else {
            automationRule.activate();
        }

        AutomationRule updated = repository.save(automationRule);

        log.info("Toggled automation rule status: {} to {} for organization: {}",
                automationRuleId, automationRule.getIsActive(), organizationId);

        return updated;
    }
}
