package com.mailist.marketing.automation.domain.valueobject;

public enum ConditionType {
    HAS_TAG("Has Tag"),
    NOT_HAS_TAG("Does Not Have Tag"),
    IN_LIST("In List"),
    NOT_IN_LIST("Not In List"),
    EMAIL_OPENED("Email Opened"),
    EMAIL_CLICKED("Email Clicked"),
    LEAD_SCORE("Lead Score"),
    FIELD_VALUE("Field Value"),
    DATE_BASED("Date Based"),
    CUSTOM_CONDITION("Custom Condition");
    
    private final String displayName;
    
    ConditionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}