package com.mailist.mailist.apikey.domain.valueobject;

/**
 * Status of an API key.
 */
public enum ApiKeyStatus {
    ACTIVE("active", "Active"),
    REVOKED("revoked", "Revoked"),
    EXPIRED("expired", "Expired");

    private final String code;
    private final String displayName;

    ApiKeyStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}
