package com.mailist.mailist.automation.domain.valueobject;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Action {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private ActionType type;
    
    @Column(name = "action_value")
    private String value;
    
    @Column(name = "action_delay_minutes")
    private Integer delayMinutes;
    
    @Column(name = "action_parameters", columnDefinition = "TEXT")
    private String parameters;
    
    public enum ActionType {
        SEND_EMAIL,
        ADD_TAG,
        REMOVE_TAG,
        MOVE_TO_LIST,
        REMOVE_FROM_LIST,
        UPDATE_LEAD_SCORE,
        WAIT,
        WEBHOOK,
        CUSTOM_ACTION
    }
}