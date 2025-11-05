package com.mailist.mailist.automation.application.port.out;

import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.domain.valueobject.TriggerType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.List;

public interface AutomationRuleRepository {
    AutomationRule save(AutomationRule automationRule);
    Optional<AutomationRule> findById(Long id);
    Page<AutomationRule> findAll(Pageable pageable);
    List<AutomationRule> findByIsActiveTrue();
    List<AutomationRule> findByTriggerType(TriggerType triggerType);
    void deleteById(Long id);
    boolean existsById(Long id);
    long countActive();
}