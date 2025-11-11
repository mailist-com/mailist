package com.mailist.mailist.notification.domain.aggregate;

import com.mailist.mailist.shared.domain.aggregate.BaseTenantEntity;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_tenant_user", columnList = "tenant_id, user_id"),
    @Index(name = "idx_is_read", columnList = "is_read"),
    @Index(name = "idx_category", columnList = "category")
})
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationCategory category;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "list_name", length = 255)
    private String listName;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "action_text", length = 100)
    private String actionText;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }

    public enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }

    public enum NotificationCategory {
        CONTACT_ADDED,
        CONTACT_UPDATED,
        CONTACT_REMOVED,
        CAMPAIGN_SENT,
        AUTOMATION_TRIGGERED,
        SYSTEM,
        BILLING
    }
}
