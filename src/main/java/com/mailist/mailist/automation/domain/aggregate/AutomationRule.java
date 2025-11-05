package com.mailist.mailist.automation.domain.aggregate;

import com.mailist.mailist.automation.domain.valueobject.Condition;
import com.mailist.mailist.automation.domain.valueobject.Action;
import com.mailist.mailist.automation.domain.valueobject.TriggerType;
import com.mailist.mailist.shared.domain.aggregate.BaseTenantEntity;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "automation_rules")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutomationRule extends BaseTenantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private TriggerType triggerType;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "automation_conditions", joinColumns = @JoinColumn(name = "rule_id"))
    @Builder.Default
    private List<Condition> conditions = new ArrayList<>();
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "automation_actions", joinColumns = @JoinColumn(name = "rule_id"))
    @Builder.Default
    private List<Action> actions = new ArrayList<>();
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "automation_else_actions", joinColumns = @JoinColumn(name = "rule_id"))
    @Builder.Default
    private List<Action> elseActions = new ArrayList<>();
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
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
    
    public void activate() {
        this.isActive = true;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void addCondition(Condition condition) {
        this.conditions.add(condition);
    }
    
    public void addAction(Action action) {
        this.actions.add(action);
    }
    
    public void addElseAction(Action action) {
        this.elseActions.add(action);
    }
    
}