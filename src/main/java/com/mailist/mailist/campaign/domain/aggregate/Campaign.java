package com.mailist.mailist.campaign.domain.aggregate;

import com.mailist.mailist.campaign.domain.valueobject.EmailTemplate;
import com.mailist.mailist.shared.domain.aggregate.BaseTenantEntity;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "campaigns")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Campaign extends BaseTenantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String subject;
    
    @Embedded
    private EmailTemplate template;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "campaign_recipients", joinColumns = @JoinColumn(name = "campaign_id"))
    @Column(name = "recipient_email")
    @Builder.Default
    private Set<String> recipients = new HashSet<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CampaignStatus status = CampaignStatus.DRAFT;
    
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
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
    
    public void schedule(LocalDateTime scheduledAt) {
        if (this.status != CampaignStatus.DRAFT) {
            throw new IllegalStateException("Campaign can only be scheduled from DRAFT status");
        }
        this.scheduledAt = scheduledAt;
        this.status = CampaignStatus.SCHEDULED;
    }
    
    public void send() {
        if (this.status != CampaignStatus.SCHEDULED && this.status != CampaignStatus.DRAFT) {
            throw new IllegalStateException("Campaign can only be sent from SCHEDULED or DRAFT status");
        }
        this.sentAt = LocalDateTime.now();
        this.status = CampaignStatus.SENT;
    }
    
    public void addRecipient(String email) {
        if (this.status == CampaignStatus.SENT) {
            throw new IllegalStateException("Cannot modify recipients of sent campaign");
        }
        this.recipients.add(email);
    }
    
    public enum CampaignStatus {
        DRAFT, SCHEDULED, SENT, FAILED
    }
}