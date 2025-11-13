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
        log.info("Fetching notifications for page: {}, size: {}, category: {}, isRead: {}",
            page, size, category, isRead);

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notifications;

        if (category != null && isRead != null) {
            NotificationCategory cat = NotificationCategory.valueOf(category.toUpperCase());
            notifications = notificationService.getNotificationsByCategoryAndReadStatus(cat, isRead, pageable);
        } else if (category != null) {
            NotificationCategory cat = NotificationCategory.valueOf(category.toUpperCase());
            notifications = notificationService.getNotificationsByCategory(cat, pageable);
        } else if (Boolean.FALSE.equals(isRead)) {
            notifications = notificationService.getUnreadNotifications(pageable);
        } else {
            notifications = notificationService.getUserNotifications(pageable);
        }

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Get specific notification details")
    ResponseEntity<NotificationDto> getNotificationById(@PathVariable Long id) {
        log.info("Fetching notification {}", id);

        NotificationDto notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(notification);
    }

    @PostMapping
    @Operation(summary = "Create notification", description = "Create a new notification (admin only)")
    ResponseEntity<NotificationDto> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        log.info("Creating notification for request: {}", request);

        NotificationDto notification = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark as read", description = "Mark notification as read")
    ResponseEntity<NotificationDto> markAsRead(@PathVariable Long id) {
        log.info("Marking notification {} as read ", id);

        NotificationDto notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/{id}/unread")
    @Operation(summary = "Mark as unread", description = "Mark notification as unread")
    ResponseEntity<NotificationDto> markAsUnread(@PathVariable Long id) {
        log.info("Marking notification {} as unread", id);

        NotificationDto notification = notificationService.markAsUnread(id);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all as read", description = "Mark all user notifications as read")
    ResponseEntity<ApiResponse> markAllAsRead() {
        log.info("Marking all notifications as read");

        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    ResponseEntity<ApiResponse> deleteNotification(@PathVariable Long id) {
        log.info("Deleting notification {}", id);

        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted"));
    }

    @DeleteMapping("/read")
    @Operation(summary = "Delete all read", description = "Delete all read notifications")
    ResponseEntity<ApiResponse> deleteAllRead() {
        log.info("Deleting all read notifications");

        notificationService.deleteAllRead();
        return ResponseEntity.ok(ApiResponse.success("All read notifications deleted"));
    }

    @GetMapping("/unread/count")
    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    ResponseEntity<Map<String, Long>> getUnreadCount() {
        log.info("Fetching unread count");

        Long count = notificationService.getUnreadCount();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get notification stats", description = "Get user notification statistics")
    ResponseEntity<NotificationStatsDto> getStats() {
        log.info("Fetching notification stats");

        NotificationStatsDto stats = notificationService.getNotificationStats();
        return ResponseEntity.ok(stats);
    }
}
