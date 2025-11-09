package com.mailist.mailist.notification.application.usecase.dto;

import com.mailist.mailist.notification.domain.aggregate.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private Long id;
    private Long userId;
    private String type;
    private String category;
    private String title;
    private String message;
    private String contactEmail;
    private String listName;
    private String actionUrl;
    private String actionText;
    private Boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private Map<String, Object> metadata;

    public static NotificationDto fromEntity(Notification notification) {
        return NotificationDto.builder()
            .id(notification.getId())
            .userId(notification.getUserId())
            .type(notification.getType().name().toLowerCase())
            .category(notification.getCategory().name().toLowerCase())
            .title(notification.getTitle())
            .message(notification.getMessage())
            .contactEmail(notification.getContactEmail())
            .listName(notification.getListName())
            .actionUrl(notification.getActionUrl())
            .actionText(notification.getActionText())
            .isRead(notification.getIsRead())
            .readAt(notification.getReadAt())
            .createdAt(notification.getCreatedAt())
            .metadata(parseMetadata(notification.getMetadata()))
            .build();
    }

    private static Map<String, Object> parseMetadata(String metadataJson) {
        // TODO: Implement JSON parsing if needed
        return null;
    }
}
