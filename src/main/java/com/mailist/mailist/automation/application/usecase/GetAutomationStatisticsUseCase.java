package com.mailist.mailist.automation.application.usecase;

import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.infrastructure.repository.AutomationRuleRepository;
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetAutomationStatisticsUseCase {

    private final AutomationRuleRepository repository;

    public record AutomationStatistics(
            long total,
            long active,
            long inactive,
            long draft
    ) {}

    @Transactional(readOnly = true)
    public AutomationStatistics execute() {
        long organizationId = TenantContext.getOrganizationId();

        // Get all automation rules for the organization
        Page<AutomationRule> allRules = repository.findAll(Pageable.unpaged());

        long total = allRules.getTotalElements();
        long active = allRules.stream()
                .filter(rule -> rule.getTenantId().equals(organizationId) && rule.getIsActive())
                .count();
        long inactive = allRules.stream()
                .filter(rule -> rule.getTenantId().equals(organizationId) && !rule.getIsActive())
                .count();

        log.debug("Retrieved automation statistics for organization: {}", organizationId);

        return new AutomationStatistics(total, active, inactive, 0);
    }
}
