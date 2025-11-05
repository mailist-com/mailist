package com.mailist.mailist.contact.domain.valueobject;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
    
    @Column(name = "tag_name")
    private String name;
    
    @Column(name = "tag_color")
    private String color;
    
    @Column(name = "tag_description")
    private String description;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(name, tag.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}