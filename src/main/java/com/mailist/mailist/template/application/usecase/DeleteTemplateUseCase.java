package com.mailist.mailist.template.application.usecase;

import com.mailist.mailist.shared.infrastructure.exception.EntityNotFoundException;
import com.mailist.mailist.template.infrastructure.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
final class DeleteTemplateUseCase {

    private final TemplateRepository templateRepository;

    void execute(final Long templateId) {
        log.info("Deleting template with ID: {}", templateId);

        // Verify template exists
        if (!templateRepository.existsById(templateId)) {
            throw new EntityNotFoundException("Template", templateId);
        }

        // Delete template
        templateRepository.deleteById(templateId);
        log.info("Template deleted successfully with ID: {}", templateId);
    }
}
