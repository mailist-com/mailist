package com.mailist.mailist.notification.application.usecase.dto;

import com.mailist.mailist.notification.domain.aggregate.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Type is required")
    private Notification.NotificationType type;

    @NotNull(message = "Category is required")
    private Notification.NotificationCategory category;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private String contactEmail;
    private String listName;
    private String actionUrl;
    private String actionText;
    private Map<String, Object> metadata;
}
