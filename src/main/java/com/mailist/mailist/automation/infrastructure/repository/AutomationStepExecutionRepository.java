package com.mailist.mailist.automation.infrastructure.repository;

import com.mailist.mailist.automation.domain.aggregate.AutomationExecution;
import com.mailist.mailist.automation.domain.aggregate.AutomationStepExecution;
import com.mailist.mailist.automation.domain.aggregate.AutomationStepExecution.StepExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AutomationStepExecutionRepository extends JpaRepository<AutomationStepExecution, Long> {

    /**
     * Find all step executions for a given automation execution
     */
    List<AutomationStepExecution> findByAutomationExecutionOrderByStartedAtAsc(AutomationExecution execution);

    /**
     * Find all step executions for a given automation execution ID
     */
    @Query("SELECT se FROM AutomationStepExecution se WHERE se.automationExecution.id = :executionId " +
           "ORDER BY se.startedAt ASC NULLS LAST")
    List<AutomationStepExecution> findByAutomationExecutionId(@Param("executionId") Long executionId);

    /**
     * Find step execution by step ID and execution ID
     */
    Optional<AutomationStepExecution> findByAutomationExecutionAndStepId(
            AutomationExecution execution, String stepId);

    /**
     * Find all scheduled step executions that should be executed now or in the past
     */
    @Query("SELECT se FROM AutomationStepExecution se WHERE se.status = 'SCHEDULED' " +
           "AND se.scheduledFor <= :currentTime ORDER BY se.scheduledFor ASC")
    List<AutomationStepExecution> findScheduledStepsReadyToExecute(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find pending step executions for a given automation execution
     */
    @Query("SELECT se FROM AutomationStepExecution se WHERE se.automationExecution.id = :executionId " +
           "AND se.status = 'PENDING' ORDER BY se.id ASC")
    List<AutomationStepExecution> findPendingByExecutionId(@Param("executionId") Long executionId);

    /**
     * Find the next pending step for a given automation execution.
     * Returns the first pending step ordered by ID (creation order).
     * IMPORTANT: Returns first result only to handle multiple pending steps correctly.
     */
    @Query(value = "SELECT * FROM automation_step_executions " +
           "WHERE automation_execution_id = :executionId AND status = 'PENDING' " +
           "ORDER BY id ASC LIMIT 1", nativeQuery = true)
    Optional<AutomationStepExecution> findNextPendingStep(@Param("executionId") Long executionId);

    /**
     * Count completed steps for a given execution
     */
    @Query("SELECT COUNT(se) FROM AutomationStepExecution se WHERE se.automationExecution.id = :executionId " +
           "AND se.status = 'COMPLETED'")
    long countCompletedByExecutionId(@Param("executionId") Long executionId);

    /**
     * Count failed steps for a given execution
     */
    @Query("SELECT COUNT(se) FROM AutomationStepExecution se WHERE se.automationExecution.id = :executionId " +
           "AND se.status = 'FAILED'")
    long countFailedByExecutionId(@Param("executionId") Long executionId);

    /**
     * Check if all steps are completed for a given execution
     */
    @Query("SELECT CASE WHEN COUNT(se) = 0 THEN true ELSE false END FROM AutomationStepExecution se " +
           "WHERE se.automationExecution.id = :executionId " +
           "AND se.status NOT IN ('COMPLETED', 'SKIPPED')")
    boolean areAllStepsCompleted(@Param("executionId") Long executionId);
}
