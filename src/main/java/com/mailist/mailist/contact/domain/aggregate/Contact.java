package com.mailist.mailist.contact.domain.aggregate;

import com.mailist.mailist.contact.domain.valueobject.Tag;
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
@Table(name = "contacts",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "email"}))
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Contact extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    /**
     * Email is now unique per organization, not globally
     */
    @Column(nullable = false)
    private String email;
    
    private String phone;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "contact_tags", joinColumns = @JoinColumn(name = "contact_id"))
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();
    
    @ManyToMany(mappedBy = "contacts", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ContactList> lists = new HashSet<>();
    
    @Column(name = "lead_score")
    @Builder.Default
    private Integer leadScore = 0;
    
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;
    
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
    
    public void addTag(Tag tag) {
        this.tags.add(tag);
        updateActivity();
    }
    
    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        updateActivity();
    }
    
    public boolean hasTag(String tagName) {
        return tags.stream().anyMatch(tag -> tag.getName().equals(tagName));
    }
    
    public void incrementLeadScore(int points) {
        this.leadScore += points;
        updateActivity();
    }
    
    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
}