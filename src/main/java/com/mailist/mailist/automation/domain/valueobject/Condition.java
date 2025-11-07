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
public class Condition {
    
    @Column(name = "condition_field")
    private String field;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_operator")
    private ConditionOperator operator;
    
    @Column(name = "condition_value")
    private String value;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type")
    private ConditionType type;
    
    public enum ConditionOperator {
        EQUALS,
        NOT_EQUALS,
        CONTAINS,
        NOT_CONTAINS,
        GREATER_THAN,
        LESS_THAN,
        HAS_TAG,
        NOT_HAS_TAG,
        IN_LIST,
        NOT_IN_LIST,
        EMAIL_OPENED,
        EMAIL_CLICKED
    }
}