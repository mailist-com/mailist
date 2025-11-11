package com.mailist.mailist.notification.interfaces.controller;

import com.mailist.mailist.notification.application.usecase.NotificationService;
import com.mailist.mailist.notification.application.usecase.dto.CreateNotificationRequest;
import com.mailist.mailist.notification.application.usecase.dto.NotificationDto;
import com.mailist.mailist.notification.application.usecase.dto.NotificationStatsDto;
import com.mailist.mailist.notification.domain.aggregate.Notification.NotificationCategory;
import com.mailist.mailist.shared.infrastructure.security.SecurityUtils;
import com.mailist.mailist.shared.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "User notification management")
class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get notifications", description = "Get paginated list of user notifications")
    ResponseEntity<Page<NotificationDto>> getNotifications(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Boolean isRead
    ) {
        Long userId = Long.parseLong(SecurityUtils.getCurrentUserId());
        log.info("Fetching notifications for user {}, page: {}, size: {}, category: {}, isRead: {}",
            userId, page, size, category, isRead);

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notifications;

        if (category != null && isRead != null) {
            NotificationCategory cat = NotificationCategory.valueOf(category.toUpperCase());
            notifications = notificationService.getNotificationsByCategoryAndReadStatus(
                userId, cat, isRead, pageable
            );
        } else if (category != null) {
            NotificationCategory cat = NotificationCategory.valueOf(category.toUpperCase());
            notifications = notificationService.getNotificationsByCategory(
                userId, cat, pageable
            );
        } else if (Boolean.FALSE.equals(isRead)) {
            notifications = notificationService.getUnreadNotifications(
                userId, pageable
            );
        } else {
            notifications = notificationService.getUserNotifications(
                userId, pageable
            );
        }

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Get specific notification details")
    ResponseEntity<NotificationDto> getNotificationById(
        @PathVariable Long id
    ) {
        Long userId = Long.parseLong(SecurityUtils.getCurrentUserId());
        log.info("Fetching notification {} for user {}", id, userId);

        NotificationDto notification = notificationService.getNotificationById(id, userId);
        return ResponseEntity.ok(notification);
    }

    @PostMapping
    @Operation(summary = "Create notification", description = "Create a new notification (admin only)")
    ResponseEntity<NotificationDto> createNotification(
        @Valid @RequestBody CreateNotificationRequest request
    ) {
        log.info("Creating notification for request: {}", request);

        NotificationDto notification = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark as read", description = "Mark notification as read")
    ResponseEntity<NotificationDto> markAsRead(
        @PathVariable Long id
    ) {
        Long userId = Long.parseLong(SecurityUtils.getCurrentUserId());
        log.info("Marking notification {} as read for user {}", id, userId);

        NotificationDto notification = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/{id}/unread")
    @Operation(summary = "Mark as unread", description = "Mark notification as unread")
    ResponseEntity<NotificationDto> markAsUnread(
        @PathVariable Long id
    ) {
        Long userId = Long.parseLong(SecurityUtils.getCurrentUserId());
        log.info("Marking notification {} as unread for user {}", id, userId);

        NotificationDto notification = notificationService.markAsUnread(id, userId);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all as read", description = "Mark all user notifications as read")
    ResponseEntity<ApiResponse> markAllAsRead() {
        Long userId = Long.parseLong(SecurityUtils.getCurrentUserId());
        log.info("Marking all notifications as read for user {}", userId);

        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    ResponseEntity<ApiResponse> deleteNotification(
        @PathVariable Long id
    ) {
        Long userId = Long.parseLong(SecurityUtils.getCurrentUserId());
        log.info("Deleting notification {} for user {}", id, userId);

        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted"));
    }

    @DeleteMapping("/read")
    @Operation(summary = "Delete all read", description = "Delete all read notifications")
    ResponseEntity<ApiResponse> deleteAllRead() {
        Long userId = Long.parseLong(SecurityUtils.getCurrentUserId());
        log.info("Deleting all read notifications for user {}", userId);

        notificationService.deleteAllRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All read notifications deleted"));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    ResponseEntity<Map<String, Long>> getUnreadCount() {
        Long userId = Long.parseLong(SecurityUtils.getCurrentUserId());
        log.info("Fetching unread count for user {}", userId);

        Long count = notificationService.getUnreadCount(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get notification stats", description = "Get user notification statistics")
    ResponseEntity<NotificationStatsDto> getStats() {
        Long userId = Long.parseLong(SecurityUtils.getCurrentUserId());
        log.info("Fetching notification stats for user {}", userId);

        NotificationStatsDto stats = notificationService.getNotificationStats(userId);
        return ResponseEntity.ok(stats);
    }
}
