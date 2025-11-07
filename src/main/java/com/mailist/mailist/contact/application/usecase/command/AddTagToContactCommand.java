package com.mailist.mailist.contact.application.usecase.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddTagToContactCommand {
    private Long contactId;
    private String tagName;
    private String tagColor;
    private String tagDescription;
}