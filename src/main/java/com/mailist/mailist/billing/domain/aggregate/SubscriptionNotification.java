package com.mailist.mailist.billing.domain.aggregate;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Subscription notification aggregate - tracks limit warning notifications
 */
@Entity
@Table(name = "subscription_notifications")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tenant/Organization ID
     */
    @Column(nullable = false)
    private Long tenantId;

    /**
     * Notification type (LIMIT_WARNING, LIMIT_EXCEEDED, PAYMENT_FAILED, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType notificationType;

    /**
     * Resource type (CONTACTS, EMAILS, USERS, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ResourceType resourceType;

    /**
     * Threshold percentage (e.g., 80, 90, 100)
     */
    @Column(nullable = false)
    private Integer thresholdPercentage;

    /**
     * Current usage count
     */
    @Column(nullable = false)
    private Integer currentUsage;

    /**
     * Limit value
     */
    @Column(nullable = false)
    private Integer limitValue;

    /**
     * Is notification sent
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isSent = false;

    /**
     * Timestamp when notification was sent
     */
    private LocalDateTime sentAt;

    /**
     * Notification message
     */
    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Mark notification as sent
     */
    public void markSent() {
        this.isSent = true;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Generate notification message
     */
    public String generateMessage() {
        return switch (notificationType) {
            case LIMIT_WARNING -> String.format(
                "Uwaga! Wykorzystałeś %d%% limitu %s (%d z %d). Rozważ zwiększenie planu.",
                thresholdPercentage,
                resourceType.getDisplayName(),
                currentUsage,
                limitValue
            );
            case LIMIT_EXCEEDED -> String.format(
                "Osiągnięto limit %s! Masz %d z %d dozwolonych. Zwiększ plan aby kontynuować.",
                resourceType.getDisplayName(),
                currentUsage,
                limitValue
            );
            case PAYMENT_FAILED -> "Płatność nie powiodła się. Sprawdź szczegóły płatności i spróbuj ponownie.";
            case SUBSCRIPTION_CANCELLED -> "Twoja subskrypcja została anulowana.";
            case SUBSCRIPTION_EXPIRED -> "Twoja subskrypcja wygasła.";
            case TRIAL_ENDING -> "Twój okres próbny kończy się wkrótce.";
        };
    }

    /**
     * Notification types
     */
    public enum NotificationType {
        LIMIT_WARNING,
        LIMIT_EXCEEDED,
        PAYMENT_FAILED,
        PAYMENT_SUCCEEDED,
        SUBSCRIPTION_CREATED,
        SUBSCRIPTION_CANCELLED,
        SUBSCRIPTION_EXPIRED,
        SUBSCRIPTION_RENEWED,
        TRIAL_STARTING,
        TRIAL_ENDING
    }

    /**
     * Resource types
     */
    public enum ResourceType {
        CONTACTS("kontaktów"),
        EMAILS("wysłanych maili"),
        USERS("użytkowników"),
        CAMPAIGNS("kampanii"),
        AUTOMATIONS("automatyzacji");

        private final String displayName;

        ResourceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
