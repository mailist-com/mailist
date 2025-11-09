package com.mailist.mailist.automation.infrastructure.repository;

import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.domain.aggregate.AutomationStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationStepRepository extends JpaRepository<AutomationStep, Long> {

    /**
     * Find all steps for a given automation rule
     */
    List<AutomationStep> findByAutomationRuleOrderByStepOrderAsc(AutomationRule automationRule);

    /**
     * Find all steps for a given automation rule ID
     */
    @Query("SELECT s FROM AutomationStep s WHERE s.automationRule.id = :ruleId ORDER BY s.stepOrder ASC")
    List<AutomationStep> findByAutomationRuleIdOrderByStepOrderAsc(@Param("ruleId") Long ruleId);

    /**
     * Delete all steps for a given automation rule
     */
    @Modifying
    @Query("DELETE FROM AutomationStep s WHERE s.automationRule.id = :ruleId")
    void deleteByAutomationRuleId(@Param("ruleId") Long ruleId);

    /**
     * Count steps for a given automation rule
     */
    long countByAutomationRule(AutomationRule automationRule);
}
