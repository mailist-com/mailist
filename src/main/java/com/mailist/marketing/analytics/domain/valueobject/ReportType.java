package com.mailist.marketing.analytics.domain.valueobject;

public enum ReportType {
    CAMPAIGN("Campaign Analytics"),
    CONTACT("Contact Analytics"),
    AUTOMATION("Automation Analytics"),
    OVERALL("Overall Analytics");
    
    private final String displayName;
    
    ReportType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}