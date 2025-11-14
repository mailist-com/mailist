package com.mailist.mailist.automation.domain.aggregate;

import com.mailist.mailist.shared.domain.aggregate.BaseTenantEntity;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Rejestr wykonania pojedynczego kroku automatyzacji.
 * Śledzi wykonanie każdego kroku w ramach AutomationExecution.
 */
@Entity
@Table(name = "automation_step_executions", indexes = {
    @Index(name = "idx_step_execution_execution", columnList = "automation_execution_id"),
    @Index(name = "idx_step_execution_step", columnList = "automation_step_id"),
    @Index(name = "idx_step_execution_status", columnList = "status"),
    @Index(name = "idx_step_execution_scheduled", columnList = "scheduled_for")
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutomationStepExecution extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "automation_execution_id", nullable = false)
    private AutomationExecution automationExecution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "automation_step_id", nullable = false)
    private AutomationStep automationStep;

    @Column(name = "step_id", nullable = false)
    private String stepId; // ID kroku z flow JSON

    @Column(name = "step_type", nullable = false)
    private String stepType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StepExecutionStatus status = StepExecutionStatus.PENDING;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor; // Dla kroków WAIT - kiedy ma się wykonać

    @Column(name = "input_data", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    @Builder.Default
    private Map<String, Object> inputData = new HashMap<>();

    @Column(name = "output_data", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    @Builder.Default
    private Map<String, Object> outputData = new HashMap<>();

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    public enum StepExecutionStatus {
        PENDING,      // Oczekuje na wykonanie
        RUNNING,      // Wykonywanie w toku
        COMPLETED,    // Zakończony pomyślnie
        FAILED,       // Zakończony błędem
        SKIPPED,      // Pominięty (np. warunek nie spełniony)
        SCHEDULED     // Zaplanowany (dla WAIT)
    }

    public void start() {
        this.status = StepExecutionStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    public void complete(Map<String, Object> output) {
        this.status = StepExecutionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        if (output != null) {
            this.outputData = output;
        }
    }

    public void fail(String errorMessage) {
        this.status = StepExecutionStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    public void skip(String reason) {
        this.status = StepExecutionStatus.SKIPPED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = reason;
    }

    public void schedule(LocalDateTime scheduledFor) {
        this.status = StepExecutionStatus.SCHEDULED;
        this.scheduledFor = scheduledFor;
    }

    public void incrementRetry() {
        this.retryCount++;
    }

    public boolean canRetry(int maxRetries) {
        return this.retryCount < maxRetries;
    }
}
