package com.mailist.mailist.notification.application.usecase;

import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.infrastructure.repository.UserRepository;
import com.mailist.mailist.notification.application.usecase.dto.CreateNotificationRequest;
import com.mailist.mailist.notification.application.usecase.dto.NotificationDto;
import com.mailist.mailist.notification.application.usecase.dto.NotificationStatsDto;
import com.mailist.mailist.notification.domain.aggregate.Notification;
import com.mailist.mailist.notification.domain.aggregate.Notification.NotificationCategory;
import com.mailist.mailist.notification.domain.repository.NotificationRepository;
import com.mailist.mailist.shared.infrastructure.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public NotificationDto createNotification(CreateNotificationRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findByEmail(currentUserId).orElseThrow();

        Notification notification = Notification.builder()
            .userId(user.getId())
            .type(request.getType())
            .category(request.getCategory())
            .title(request.getTitle())
            .message(request.getMessage())
            .contactEmail(request.getContactEmail())
            .listName(request.getListName())
            .actionUrl(request.getActionUrl())
            .actionText(request.getActionText())
            .isRead(false)
            .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Created notification {} for user {}", saved.getId(), saved.getUserId());

        return NotificationDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(NotificationDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getUnreadNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
            .map(NotificationDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsByCategory(
        Long userId,
        NotificationCategory category,
        Pageable pageable
    ) {
        return notificationRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(userId, category, pageable)
            .map(NotificationDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsByCategoryAndReadStatus(
        Long userId,
        NotificationCategory category,
        Boolean isRead,
        Pageable pageable
    ) {
        return notificationRepository.findByUserIdAndIsReadAndCategoryOrderByCreatedAtDesc(
            userId, isRead, category, pageable
        ).map(NotificationDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public NotificationDto getNotificationById(Long id, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        return NotificationDto.fromEntity(notification);
    }

    @Transactional
    public NotificationDto markAsRead(Long id, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.markAsRead();
        Notification updated = notificationRepository.save(notification);

        return NotificationDto.fromEntity(updated);
    }

    @Transactional
    public NotificationDto markAsUnread(Long id, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.markAsUnread();
        Notification updated = notificationRepository.save(notification);

        return NotificationDto.fromEntity(updated);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        int updated = notificationRepository.markAllAsReadForUser(userId);
        log.info("Marked {} notifications as read for user {}", updated, userId);
    }

    @Transactional
    public void deleteNotification(Long id, Long userId) {
        Notification notification = notificationRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        notificationRepository.delete(notification);
        log.info("Deleted notification {} for user {}", id, userId);
    }

    @Transactional
    public void deleteAllRead(Long userId) {
        int deleted = notificationRepository.deleteAllReadForUser(userId);
        log.info("Deleted {} read notifications for user {}", deleted, userId);
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional(readOnly = true)
    public NotificationStatsDto getNotificationStats(Long userId) {
        Long total = notificationRepository.countByUserId(userId);
        Long unread = notificationRepository.countByUserIdAndIsReadFalse(userId);

        Map<String, Long> byCategory = new HashMap<>();
        Arrays.stream(NotificationCategory.values()).forEach(category -> {
            Long count = notificationRepository.countByUserIdAndCategory(userId, category);
            byCategory.put(category.name().toLowerCase(), count);
        });

        return NotificationStatsDto.builder()
            .total(total)
            .unread(unread)
            .byCategory(byCategory)
            .build();
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getRecentNotifications(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return notificationRepository.findRecentByUserId(userId, pageable)
            .map(NotificationDto::fromEntity);
    }
}
