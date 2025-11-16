package com.mailist.mailist.template.domain.valueobject;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomField {
    private String key;
    private String label;
    private String sourceField; // Database field to fetch value from (e.g., 'firstName', 'email')
    private String defaultValue;
    private String description;
}
