package com.mailist.mailist.automation.infrastructure.repository;

import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import com.mailist.mailist.automation.domain.valueobject.TriggerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {

    List<AutomationRule> findByIsActiveTrue();

    List<AutomationRule> findByTriggerType(TriggerType triggerType);

    @Query("SELECT ar FROM AutomationRule ar WHERE ar.triggerType = :triggerType AND ar.isActive = true")
    List<AutomationRule> findActiveByTriggerType(@Param("triggerType") TriggerType triggerType);

    @Query("SELECT ar FROM AutomationRule ar WHERE ar.isActive = true")
    List<AutomationRule> findAllActive();

    @Query("SELECT COUNT(ar) FROM AutomationRule ar WHERE ar.isActive = true")
    long countActive();
}