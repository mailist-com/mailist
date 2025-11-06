package com.mailist.mailist.contact.application.usecase;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExportContactsCommand {
    private Long listId;
    private String format; // "csv"
    private List<String> fields; // List of fields to export
}
