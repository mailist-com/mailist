package com.mailist.mailist.template.application.command;

import com.mailist.mailist.template.domain.valueobject.CustomField;
import com.mailist.mailist.template.domain.valueobject.TemplateCategory;
import com.mailist.mailist.template.domain.valueobject.TemplateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTemplateCommand {
    private String name;
    private String subject;
    private TemplateCategory category;
    private Set<String> tags;
    private String htmlContent;
    private String textContent;
    private List<CustomField> customFields;
    private TemplateStatus status;
    private String thumbnailUrl;
    private String createdBy;
    private Boolean isDefault;
}
