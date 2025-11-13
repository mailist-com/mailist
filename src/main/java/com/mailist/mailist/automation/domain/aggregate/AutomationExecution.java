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
 * Rejestr wykonania automatyzacji.
 * Śledzi pojedyncze uruchomienie automatyzacji dla konkretnego kontaktu.
 */
@Entity
@Table(name = "automation_executions", indexes = {
    @Index(name = "idx_automation_execution_rule", columnList = "automation_rule_id"),
    @Index(name = "idx_automation_execution_contact", columnList = "contact_id"),
    @Index(name = "idx_automation_execution_status", columnList = "status"),
    @Index(name = "idx_automation_execution_started", columnList = "started_at")
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutomationExecution extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "automation_rule_id", nullable = false)
    private AutomationRule automationRule;

    @Column(name = "contact_id", nullable = false)
    private Long contactId;

    @Column(name = "contact_email")
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ExecutionStatus status = ExecutionStatus.RUNNING;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "context", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    @Builder.Default
    private Map<String, Object> context = new HashMap<>();

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "current_step_id")
    private String currentStepId;

    public enum ExecutionStatus {
        RUNNING,      // Wykonywanie w toku
        WAITING,      // Oczekuje (np. na delay w kroku WAIT)
        COMPLETED,    // Zakończone pomyślnie
        FAILED,       // Zakończone błędem
        CANCELLED     // Anulowane
    }

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    public void complete() {
        this.status = ExecutionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.currentStepId = null;
    }

    public void fail(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    public void cancel() {
        this.status = ExecutionStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }

    public void waitForDelay() {
        this.status = ExecutionStatus.WAITING;
    }

    public void resume() {
        this.status = ExecutionStatus.RUNNING;
    }

    public void updateCurrentStep(String stepId) {
        this.currentStepId = stepId;
    }

    public void addToContext(String key, Object value) {
        if (this.context == null) {
            this.context = new HashMap<>();
        }
        this.context.put(key, value);
    }

    public boolean isActive() {
        return status == ExecutionStatus.RUNNING || status == ExecutionStatus.WAITING;
    }
}
