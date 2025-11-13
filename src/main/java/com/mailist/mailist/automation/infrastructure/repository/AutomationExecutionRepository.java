package com.mailist.mailist.automation.infrastructure.repository;

import com.mailist.mailist.automation.domain.aggregate.AutomationExecution;
import com.mailist.mailist.automation.domain.aggregate.AutomationExecution.ExecutionStatus;
import com.mailist.mailist.automation.domain.aggregate.AutomationRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AutomationExecutionRepository extends JpaRepository<AutomationExecution, Long> {

    /**
     * Find all executions for a given automation rule
     */
    Page<AutomationExecution> findByAutomationRuleOrderByStartedAtDesc(
            AutomationRule automationRule, Pageable pageable);

    /**
     * Find all executions for a given automation rule ID
     */
    @Query("SELECT e FROM AutomationExecution e WHERE e.automationRule.id = :ruleId ORDER BY e.startedAt DESC")
    Page<AutomationExecution> findByAutomationRuleId(@Param("ruleId") Long ruleId, Pageable pageable);

    /**
     * Find all executions for a given contact
     */
    List<AutomationExecution> findByContactIdOrderByStartedAtDesc(Long contactId);

    /**
     * Find active executions (RUNNING or WAITING) for a given automation rule and contact
     */
    @Query("SELECT e FROM AutomationExecution e WHERE e.automationRule.id = :ruleId " +
           "AND e.contactId = :contactId AND e.status IN ('RUNNING', 'WAITING')")
    List<AutomationExecution> findActiveExecutions(@Param("ruleId") Long ruleId, @Param("contactId") Long contactId);

    /**
     * Find all executions by status
     */
    List<AutomationExecution> findByStatus(ExecutionStatus status);

    /**
     * Find executions waiting and scheduled to run
     */
    @Query("SELECT e FROM AutomationExecution e WHERE e.status = 'WAITING'")
    List<AutomationExecution> findAllWaiting();

    /**
     * Count executions for a given automation rule
     */
    long countByAutomationRule(AutomationRule automationRule);

    /**
     * Count successful executions for a given automation rule
     */
    @Query("SELECT COUNT(e) FROM AutomationExecution e WHERE e.automationRule.id = :ruleId AND e.status = 'COMPLETED'")
    long countSuccessfulByAutomationRuleId(@Param("ruleId") Long ruleId);

    /**
     * Count failed executions for a given automation rule
     */
    @Query("SELECT COUNT(e) FROM AutomationExecution e WHERE e.automationRule.id = :ruleId AND e.status = 'FAILED'")
    long countFailedByAutomationRuleId(@Param("ruleId") Long ruleId);

    /**
     * Check if automation rule has any active executions
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM AutomationExecution e " +
           "WHERE e.automationRule.id = :ruleId AND e.status IN ('RUNNING', 'WAITING')")
    boolean hasActiveExecutions(@Param("ruleId") Long ruleId);

    /**
     * Check if automation rule has any executions at all
     */
    boolean existsByAutomationRuleId(Long ruleId);
}
