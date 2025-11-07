package com.mailist.mailist.apikey.interfaces.dto;

import com.mailist.mailist.apikey.domain.aggregate.ApiKeyActivity;

import java.time.LocalDateTime;

/**
 * DTO for API Key Activity.
 */
public record ApiKeyActivityDto(
        Long id,
        Long apiKeyId,
        String endpoint,
        String method,
        Integer statusCode,
        Long responseTime,
        String ipAddress,
        String userAgent,
        String errorMessage,
        LocalDateTime timestamp
) {
    public static ApiKeyActivityDto fromEntity(ApiKeyActivity entity) {
        return new ApiKeyActivityDto(
                entity.getId(),
                entity.getApiKeyId(),
                entity.getEndpoint(),
                entity.getMethod(),
                entity.getStatusCode(),
                entity.getResponseTime(),
                entity.getIpAddress(),
                entity.getUserAgent(),
                entity.getErrorMessage(),
                entity.getTimestamp()
        );
    }
}
