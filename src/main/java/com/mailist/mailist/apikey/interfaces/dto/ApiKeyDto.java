package com.mailist.mailist.apikey.interfaces.dto;

import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import com.mailist.mailist.apikey.domain.valueobject.ApiKeyStatus;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for API Key (without sensitive data).
 */
public record ApiKeyDto(
        Long id,
        String name,
        String description,
        String key, // Masked key for display (e.g., "ml_live_...abc123")
        String prefix,
        ApiKeyStatus status,
        Set<String> permissions,
        LocalDateTime lastUsedAt,
        String lastUsedIpAddress,
        Long totalCalls,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ApiKeyDto fromEntity(ApiKey entity) {
        return new ApiKeyDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getDisplayKey(),
                entity.getKeyPrefix(),
                entity.getStatus(),
                entity.getPermissions(),
                entity.getLastUsedAt(),
                entity.getLastUsedIpAddress(),
                entity.getTotalCalls(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
