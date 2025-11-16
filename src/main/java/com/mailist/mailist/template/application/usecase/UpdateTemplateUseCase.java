package com.mailist.mailist.template.application.usecase;

import com.mailist.mailist.shared.infrastructure.exception.EntityNotFoundException;
import com.mailist.mailist.template.application.command.UpdateTemplateCommand;
import com.mailist.mailist.template.domain.aggregate.Template;
import com.mailist.mailist.template.infrastructure.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
final class UpdateTemplateUseCase {

    private final TemplateRepository templateRepository;

    Template execute(final UpdateTemplateCommand command) {
        log.info("Updating template with ID: {}", command.getId());

        // Find existing template
        final Template template = templateRepository.findById(command.getId())
                .orElseThrow(() -> new EntityNotFoundException("Template", command.getId()));

        // Update fields if provided
        if (command.getName() != null) {
            // Check if new name is unique (Hibernate filters by tenantId automatically)
            templateRepository.findByName(command.getName())
                    .ifPresent(existingTemplate -> {
                        if (!existingTemplate.getId().equals(command.getId())) {
                            throw new IllegalArgumentException("Template with name '" + command.getName() + "' already exists");
                        }
                    });
            template.setName(command.getName());
        }

        if (command.getSubject() != null) {
            template.setSubject(command.getSubject());
        }

        if (command.getCategory() != null) {
            template.setCategory(command.getCategory());
        }

        if (command.getTags() != null) {
            template.setTags(command.getTags());
        }

        if (command.getHtmlContent() != null) {
            template.setHtmlContent(command.getHtmlContent());
        }

        if (command.getTextContent() != null) {
            template.setTextContent(command.getTextContent());
        }

        if (command.getCustomFields() != null) {
            template.setCustomFields(command.getCustomFields());
        }

        if (command.getStatus() != null) {
            template.setStatus(command.getStatus());
        }

        if (command.getThumbnailUrl() != null) {
            template.setThumbnailUrl(command.getThumbnailUrl());
        }

        if (command.getIsDefault() != null) {
            template.setIsDefault(command.getIsDefault());
        }

        // Save updated template
        final Template savedTemplate = templateRepository.save(template);
        log.info("Template updated successfully with ID: {}", savedTemplate.getId());

        return savedTemplate;
    }
}
