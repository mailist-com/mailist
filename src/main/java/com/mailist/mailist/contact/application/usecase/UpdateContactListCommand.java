package com.mailist.mailist.contact.application.usecase;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UpdateContactListCommand {
    private Long id;
    private String name;
    private String description;
    private Boolean isSmartList;
    private String segmentRule; // JSON string with smart list conditions
    private Set<String> tags;
}
