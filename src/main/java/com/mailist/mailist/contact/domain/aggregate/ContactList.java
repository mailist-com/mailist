package com.mailist.mailist.contact.domain.aggregate;

import com.mailist.mailist.shared.domain.aggregate.BaseTenantEntity;
import com.mailist.mailist.shared.domain.aggregate.Organization;
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
@Table(name = "contact_lists")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContactList extends BaseTenantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Organization this contact list belongs to (tenant isolation)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "contact_list_contacts",
        joinColumns = @JoinColumn(name = "list_id"),
        inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    private Set<Contact> contacts = new HashSet<>();
    
    @Column(name = "is_dynamic")
    private Boolean isDynamic = false;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "segment_rule", columnDefinition = "TEXT")
    private String segmentRule;
    
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
    
    public void addContact(Contact contact) {
        if (isDynamic) {
            throw new IllegalStateException("Cannot manually add contacts to dynamic list");
        }
        this.contacts.add(contact);
        contact.getLists().add(this);
    }
    
    public void removeContact(Contact contact) {
        if (isDynamic) {
            throw new IllegalStateException("Cannot manually remove contacts from dynamic list");
        }
        this.contacts.remove(contact);
        contact.getLists().remove(this);
    }
    
    public int getContactCount() {
        return contacts.size();
    }
}