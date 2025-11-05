package com.mailist.mailist.automation.domain.valueobject;

public enum TriggerType {
    CONTACT_CREATED("Contact Created"),
    CONTACT_TAGGED("Contact Tagged"),
    TAG_ADDED("Tag Added"),
    CONTACT_JOINED_LIST("Contact Joined List"),
    EMAIL_OPENED("Email Opened"),
    EMAIL_CLICKED("Email Clicked"),
    LEAD_SCORE_CHANGED("Lead Score Changed"),
    DATE_BASED("Date Based"),
    WEBHOOK_RECEIVED("Webhook Received"),
    MANUAL_TRIGGER("Manual Trigger");
    
    private final String displayName;
    
    TriggerType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}