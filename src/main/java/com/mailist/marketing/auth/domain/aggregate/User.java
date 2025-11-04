package com.mailist.marketing.auth.domain.aggregate;

import com.mailist.marketing.shared.domain.aggregate.BaseTenantEntity;
import com.mailist.marketing.shared.domain.aggregate.Organization;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseTenantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = Set.of(Role.USER);
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING_VERIFICATION;
    
    @Column(name = "verification_token")
    private String verificationToken;
    
    @Column(name = "verification_token_expires_at")
    private LocalDateTime verificationTokenExpiresAt;
    
    @Column(name = "password_reset_token")
    private String passwordResetToken;
    
    @Column(name = "password_reset_token_expires_at")
    private LocalDateTime passwordResetTokenExpiresAt;
    
    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;
    
    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void updateLastLogin() {
        lastLoginAt = LocalDateTime.now();
        failedLoginAttempts = 0;
        accountLockedUntil = null;
    }
    
    public void incrementFailedLoginAttempts() {
        failedLoginAttempts++;
        if (failedLoginAttempts >= 5) {
            accountLockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }
    
    public boolean isAccountLocked() {
        return accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now());
    }
    
    public void verifyEmail() {
        this.emailVerified = true;
        this.status = Status.ACTIVE;
        this.verificationToken = null;
        this.verificationTokenExpiresAt = null;
    }
    
    public boolean isVerificationTokenValid() {
        return verificationToken != null && 
               verificationTokenExpiresAt != null && 
               verificationTokenExpiresAt.isAfter(LocalDateTime.now());
    }
    
    public boolean isPasswordResetTokenValid() {
        return passwordResetToken != null && 
               passwordResetTokenExpiresAt != null && 
               passwordResetTokenExpiresAt.isAfter(LocalDateTime.now());
    }
    
    public void setPasswordResetToken(String token) {
        this.passwordResetToken = token;
        this.passwordResetTokenExpiresAt = LocalDateTime.now().plusHours(1);
    }
    
    public void setVerificationToken(String token) {
        this.verificationToken = token;
        this.verificationTokenExpiresAt = LocalDateTime.now().plusDays(1);
    }
    
    public void resetPassword(String newPassword) {
        this.password = newPassword;
        this.passwordResetToken = null;
        this.passwordResetTokenExpiresAt = null;
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }
    
    public boolean hasRole(Role role) {
        return roles.contains(role);
    }
    
    public boolean isAdmin() {
        return roles.contains(Role.ADMIN);
    }
    
    public boolean isOwner() {
        return roles.contains(Role.OWNER);
    }
    
    public enum Role {
        OWNER,    // Organization owner
        ADMIN,    // Organization admin
        USER      // Regular user
    }
    
    public enum Status {
        PENDING_VERIFICATION,
        ACTIVE,
        INACTIVE,
        SUSPENDED
    }
}