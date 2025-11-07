package com.mailist.mailist.contact.application.usecase.command;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Data
@Builder
public class ImportContactsCommand {
    private Long listId;
    private MultipartFile file;
    private Map<String, String> mapping; // Maps CSV column names to field names
    private Boolean skipDuplicates;
}
