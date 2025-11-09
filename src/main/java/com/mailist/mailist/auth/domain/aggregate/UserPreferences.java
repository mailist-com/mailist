package com.mailist.mailist.auth.domain.aggregate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Notification preferences
    @Column(name = "email_notifications")
    @Builder.Default
    private Boolean emailNotifications = true;

    @Column(name = "campaign_updates")
    @Builder.Default
    private Boolean campaignUpdates = true;

    @Column(name = "automation_alerts")
    @Builder.Default
    private Boolean automationAlerts = true;

    @Column(name = "monthly_reports")
    @Builder.Default
    private Boolean monthlyReports = true;

    @Column(name = "system_updates")
    @Builder.Default
    private Boolean systemUpdates = false;

    // Email preferences
    @Column(name = "default_from_name")
    private String defaultFromName;

    @Column(name = "default_from_email")
    private String defaultFromEmail;

    @Column(name = "email_signature", length = 1000)
    private String emailSignature;

    // Display preferences
    @Column(name = "date_format")
    @Builder.Default
    private String dateFormat = "DD.MM.YYYY";

    @Column(name = "time_format")
    @Builder.Default
    private String timeFormat = "24h";

    @Column(name = "dark_mode")
    @Builder.Default
    private Boolean darkMode = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
