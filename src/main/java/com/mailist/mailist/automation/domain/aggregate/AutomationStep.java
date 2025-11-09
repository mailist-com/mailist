package com.mailist.mailist.automation.domain.aggregate;

import com.mailist.mailist.shared.domain.aggregate.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "automation_steps")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutomationStep extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "automation_rule_id", nullable = false)
    private AutomationRule automationRule;

    @Column(name = "step_id", nullable = false)
    private String stepId; // ID węzła z JSON

    @Column(name = "step_type", nullable = false)
    private String stepType; // Typ węzła (np. SEND_EMAIL, WAIT, etc.)

    @Column(name = "step_order")
    private Integer stepOrder; // Kolejność kroku w flow

    @Column(name = "position_x")
    private Double positionX;

    @Column(name = "position_y")
    private Double positionY;

    @Column(name = "input_connection_id")
    private String inputConnectionId; // ID wejściowego połączenia

    @Column(name = "output_connections", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    @Builder.Default
    private Map<String, String> outputConnections = new HashMap<>(); // Mapowanie output ID -> nazwa

    @Column(name = "settings", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    @Builder.Default
    private Map<String, Object> settings = new HashMap<>(); // Ustawienia węzła jako mapa klucz/wartość

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "is_expanded")
    @Builder.Default
    private Boolean isExpanded = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addSetting(String key, Object value) {
        this.settings.put(key, value);
    }

    public void addOutputConnection(String outputId, String outputName) {
        this.outputConnections.put(outputId, outputName);
    }
}
