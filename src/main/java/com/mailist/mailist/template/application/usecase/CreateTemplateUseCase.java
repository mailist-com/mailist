package com.mailist.mailist.template.application.usecase;

import com.mailist.mailist.template.application.command.CreateTemplateCommand;
import com.mailist.mailist.template.domain.aggregate.Template;
import com.mailist.mailist.template.domain.valueobject.TemplateStatus;
import com.mailist.mailist.template.infrastructure.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
final class CreateTemplateUseCase {

    private final TemplateRepository templateRepository;

    Template execute(final CreateTemplateCommand command) {
        log.info("Creating new template with name: {}", command.getName());

        // Check if template with same name already exists (Hibernate filters by tenantId automatically)
        if (templateRepository.existsByName(command.getName())) {
            throw new IllegalArgumentException("Template with name '" + command.getName() + "' already exists");
        }

        // Build template entity
        final Template template = Template.builder()
                .name(command.getName())
                .subject(command.getSubject())
                .category(command.getCategory())
                .tags(command.getTags())
                .htmlContent(command.getHtmlContent())
                .textContent(command.getTextContent())
                .customFields(command.getCustomFields())
                .status(command.getStatus() != null ? command.getStatus() : TemplateStatus.DRAFT)
                .thumbnailUrl(command.getThumbnailUrl())
                .createdBy(command.getCreatedBy())
                .isDefault(command.getIsDefault() != null ? command.getIsDefault() : false)
                .usageCount(0)
                .campaignsCount(0)
                .build();

        // Save template
        final Template savedTemplate = templateRepository.save(template);
        log.info("Template created successfully with ID: {}", savedTemplate.getId());

        return savedTemplate;
    }
}
