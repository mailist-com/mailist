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
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
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

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationDto createNotification(CreateNotificationRequest request) {
        User user = getUser();

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

    public User getUser() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Long organizationId = TenantContext.getOrganizationId();
        User user = userRepository.findByEmail(currentUserId).orElseThrow();
        return user;
    }

    public Page<NotificationDto> getUserNotifications(Pageable pageable) {
        User user = getUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
            .map(NotificationDto::fromEntity);
    }

    public Page<NotificationDto> getUnreadNotifications(Pageable pageable) {
        User user = getUser();
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId(), pageable)
            .map(NotificationDto::fromEntity);
    }

    public Page<NotificationDto> getNotificationsByCategory(NotificationCategory category, Pageable pageable) {
        User user = getUser();
        return notificationRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(user.getId(), category, pageable)
            .map(NotificationDto::fromEntity);
    }

    public Page<NotificationDto> getNotificationsByCategoryAndReadStatus(NotificationCategory category, Boolean isRead, Pageable pageable) {
        User user = getUser();

        return notificationRepository.findByUserIdAndIsReadAndCategoryOrderByCreatedAtDesc(user.getId(), isRead, category, pageable)
                .map(NotificationDto::fromEntity);
    }

    public NotificationDto getNotificationById(Long id) {
        User user = getUser();

        Notification notification = notificationRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        return NotificationDto.fromEntity(notification);
    }

    public NotificationDto markAsRead(Long id) {
        User user = getUser();

        Notification notification = notificationRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.markAsRead();
        Notification updated = notificationRepository.save(notification);

        return NotificationDto.fromEntity(updated);
    }

    public NotificationDto markAsUnread(Long id) {
        User user = getUser();

        Notification notification = notificationRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.markAsUnread();
        Notification updated = notificationRepository.save(notification);

        return NotificationDto.fromEntity(updated);
    }

    public void markAllAsRead() {
        User user = getUser();

        int updated = notificationRepository.markAllAsReadForUser(user.getId());
        log.info("Marked {} notifications as read for user {}", updated, user.getId());
    }

    public void deleteNotification(Long id) {
        User user = getUser();

        Notification notification = notificationRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new RuntimeException("Notification not found"));

        notificationRepository.delete(notification);
        log.info("Deleted notification {}", id);
    }

    public void deleteAllRead() {
        User user = getUser();

        int deleted = notificationRepository.deleteAllReadForUser(user.getId());
        log.info("Deleted {} read notifications for user {}", deleted, user.getId());
    }

    public Long getUnreadCount() {
        User user = getUser();

        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    public NotificationStatsDto getNotificationStats() {
        User user = getUser();

        Long total = notificationRepository.countByUserId(user.getId());
        Long unread = notificationRepository.countByUserIdAndIsReadFalse(user.getId());

        Map<String, Long> byCategory = new HashMap<>();
        Arrays.stream(NotificationCategory.values()).forEach(category -> {
            Long count = notificationRepository.countByUserIdAndCategory(user.getId(), category);
            byCategory.put(category.name().toLowerCase(), count);
        });

        return NotificationStatsDto.builder()
            .total(total)
            .unread(unread)
            .byCategory(byCategory)
            .build();
    }
}
