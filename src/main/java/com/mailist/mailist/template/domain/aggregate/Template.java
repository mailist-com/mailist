package com.mailist.mailist.template.domain.aggregate;

import com.mailist.mailist.shared.domain.aggregate.BaseTenantEntity;
import com.mailist.mailist.template.domain.valueobject.CustomField;
import com.mailist.mailist.template.domain.valueobject.TemplateCategory;
import com.mailist.mailist.template.domain.valueobject.TemplateStatus;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "templates",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "name"}))
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Template extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 500)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TemplateCategory category;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "template_tags", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @Column(name = "html_content", columnDefinition = "LONGTEXT", nullable = false)
    private String htmlContent;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_fields", columnDefinition = "jsonb")
    @Builder.Default
    private List<CustomField> customFields = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private TemplateStatus status = TemplateStatus.DRAFT;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "campaigns_count")
    @Builder.Default
    private Integer campaignsCount = 0;

    @Column(name = "avg_open_rate")
    private Double avgOpenRate;

    @Column(name = "avg_click_rate")
    private Double avgClickRate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Domain methods
    public void activate() {
        this.status = TemplateStatus.ACTIVE;
        updateTimestamp();
    }

    public void deactivate() {
        this.status = TemplateStatus.ARCHIVED;
        updateTimestamp();
    }

    public void setAsDraft() {
        this.status = TemplateStatus.DRAFT;
        updateTimestamp();
    }

    public void incrementUsage() {
        this.usageCount++;
        this.lastUsedAt = LocalDateTime.now();
        updateTimestamp();
    }

    public void updateStatistics(int campaignsCount, Double openRate, Double clickRate) {
        this.campaignsCount = campaignsCount;
        this.avgOpenRate = openRate;
        this.avgClickRate = clickRate;
        updateTimestamp();
    }

    public boolean isActive() {
        return TemplateStatus.ACTIVE.equals(this.status);
    }

    public boolean isDraft() {
        return TemplateStatus.DRAFT.equals(this.status);
    }

    public boolean isArchived() {
        return TemplateStatus.ARCHIVED.equals(this.status);
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
