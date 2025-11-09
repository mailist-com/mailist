package com.mailist.mailist.automation.application.usecase;

import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.infrastructure.repository.AutomationRuleRepository;
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DuplicateAutomationRuleUseCase {

    private final AutomationRuleRepository repository;

    @Transactional
    public AutomationRule execute(Long automationRuleId) {
        long organizationId = TenantContext.getOrganizationId();

        AutomationRule original = repository.findById(automationRuleId)
                .filter(rule -> rule.getTenantId().equals(organizationId))
                .orElseThrow(() -> new IllegalArgumentException("Automation rule not found: " + automationRuleId));

        AutomationRule duplicate = AutomationRule.builder()
                .name(original.getName() + " (kopia)")
                .description(original.getDescription())
                .triggerType(original.getTriggerType())
                .flowJson(original.getFlowJson())
                .isActive(false)  // Start duplicates as inactive
                .build();

        duplicate.setTenantId(organizationId);

        AutomationRule saved = repository.save(duplicate);

        log.info("Duplicated automation rule: {} to new ID: {} for organization: {}",
                automationRuleId, saved.getId(), organizationId);

        // TODO: Duplicate automation steps as well
        // Need to parse flowJson and create new AutomationStep records for the duplicate

        return saved;
    }
}
