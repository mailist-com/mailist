package com.mailist.marketing.analytics.domain.valueobject;

public enum ReportFormat {
    PDF("application/pdf", ".pdf"),
    CSV("text/csv", ".csv"),
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    JSON("application/json", ".json");
    
    private final String mimeType;
    private final String fileExtension;
    
    ReportFormat(String mimeType, String fileExtension) {
        this.mimeType = mimeType;
        this.fileExtension = fileExtension;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public String getFileExtension() {
        return fileExtension;
    }
}