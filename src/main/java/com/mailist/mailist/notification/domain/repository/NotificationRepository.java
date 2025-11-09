package com.mailist.mailist.notification.domain.repository;

import com.mailist.mailist.notification.domain.aggregate.Notification;
import com.mailist.mailist.notification.domain.aggregate.Notification.NotificationCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a specific user with pagination
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find unread notifications for a user
     */
    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find notifications by category
     */
    Page<Notification> findByUserIdAndCategoryOrderByCreatedAtDesc(
        Long userId,
        NotificationCategory category,
        Pageable pageable
    );

    /**
     * Find notifications by read status and category
     */
    Page<Notification> findByUserIdAndIsReadAndCategoryOrderByCreatedAtDesc(
        Long userId,
        Boolean isRead,
        NotificationCategory category,
        Pageable pageable
    );

    /**
     * Count all notifications for a user
     */
    Long countByUserId(Long userId);

    /**
     * Count unread notifications for a user
     */
    Long countByUserIdAndIsReadFalse(Long userId);

    /**
     * Count notifications by category for a user
     */
    Long countByUserIdAndCategory(Long userId, NotificationCategory category);

    /**
     * Find notification by ID and user ID (for security)
     */
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    /**
     * Mark all notifications as read for a user
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadForUser(@Param("userId") Long userId);

    /**
     * Delete all read notifications for a user
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.userId = :userId AND n.isRead = true")
    int deleteAllReadForUser(@Param("userId") Long userId);

    /**
     * Find recent notifications (limited count)
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.createdAt DESC")
    Page<Notification> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);
}
