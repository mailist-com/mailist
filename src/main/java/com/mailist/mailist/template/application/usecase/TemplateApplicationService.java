package com.mailist.mailist.template.application.usecase;

import com.mailist.mailist.template.application.command.CreateTemplateCommand;
import com.mailist.mailist.template.application.command.UpdateTemplateCommand;
import com.mailist.mailist.template.domain.aggregate.Template;
import com.mailist.mailist.template.infrastructure.repository.TemplateRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TemplateApplicationService {

    private final CreateTemplateUseCase createTemplateUseCase;
    private final UpdateTemplateUseCase updateTemplateUseCase;
    private final DeleteTemplateUseCase deleteTemplateUseCase;
    private final TemplateRepository templateRepository;

    public Template create(final CreateTemplateCommand command) {
        return createTemplateUseCase.execute(command);
    }

    public Template update(final UpdateTemplateCommand command) {
        return updateTemplateUseCase.execute(command);
    }

    public void delete(final Long templateId) {
        deleteTemplateUseCase.execute(templateId);
    }

    /**
     * Get template by ID - used by automation engine
     * Hibernate automatically filters by tenantId using @TenantId annotation
     */
    public Optional<Template> getTemplateById(final Long templateId) {
        return templateRepository.findById(templateId);
    }
}
