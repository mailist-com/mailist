package com.mailist.mailist.contact.application.usecase;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateContactListCommand {
    private String name;
    private String description;
    private Boolean isSmartList;
    private String segmentRule; // JSON string with smart list conditions
}
