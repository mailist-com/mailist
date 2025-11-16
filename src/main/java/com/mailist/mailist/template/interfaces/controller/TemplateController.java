package com.mailist.mailist.template.interfaces.controller;

import com.mailist.mailist.shared.interfaces.dto.ApiResponse;
import com.mailist.mailist.shared.interfaces.dto.PagedResponse;
import com.mailist.mailist.template.application.command.CreateTemplateCommand;
import com.mailist.mailist.template.application.command.UpdateTemplateCommand;
import com.mailist.mailist.template.application.usecase.TemplateApplicationService;
import com.mailist.mailist.template.domain.aggregate.Template;
import com.mailist.mailist.template.domain.valueobject.TemplateCategory;
import com.mailist.mailist.template.domain.valueobject.TemplateStatus;
import com.mailist.mailist.template.infrastructure.repository.TemplateRepository;
import com.mailist.mailist.template.interfaces.dto.TemplateDto;
import com.mailist.mailist.template.interfaces.mapper.TemplateMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Template Management", description = "Email template CRUD operations and management")
class TemplateController {

    private final TemplateApplicationService templateApplicationService;
    private final TemplateRepository templateRepository;
    private final TemplateMapper templateMapper;

    @PostMapping
    @Operation(summary = "Create a new template")
    ResponseEntity<ApiResponse<TemplateDto.Response>> createTemplate(
            @Valid @RequestBody final TemplateDto.CreateRequest request) {
        log.info("Creating new template with name: {}", request.getName());

        final CreateTemplateCommand command = templateMapper.toCreateCommand(request);
        final Template template = templateApplicationService.create(command);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(templateMapper.toResponse(template), "Template created successfully"));
    }

    @GetMapping
    @Operation(summary = "List all templates with pagination and optional filtering")
    ResponseEntity<ApiResponse<PagedResponse<TemplateDto.Response>>> listTemplates(
            @RequestParam(required = false) final TemplateStatus status,
            @RequestParam(required = false) final TemplateCategory category,
            @RequestParam(required = false) final String search,
            final Pageable pageable) {
        log.info("Listing templates - page: {}, size: {}, status: {}, category: {}, search: {}",
                pageable.getPageNumber(), pageable.getPageSize(), status, category, search);

        Page<Template> templatesPage;

        if (search != null && !search.isEmpty()) {
            // Search by name or subject (Hibernate filters by tenantId automatically)
            templatesPage = templateRepository.search(search, pageable);
        } else if (status != null) {
            // Filter by status (Hibernate filters by tenantId automatically)
            templatesPage = templateRepository.findByStatus(status, pageable);
        } else if (category != null) {
            // Filter by category (Hibernate filters by tenantId automatically)
            templatesPage = templateRepository.findByCategory(category, pageable);
        } else {
            // Get all templates (Hibernate filters by tenantId automatically)
            templatesPage = templateRepository.findAll(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(templatesPage, templateMapper::toResponse)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get template by ID")
    ResponseEntity<ApiResponse<TemplateDto.Response>> getTemplate(@PathVariable final Long id) {
        log.info("Getting template with ID: {}", id);

        final Optional<Template> template = templateRepository.findById(id);

        return template.map(value -> ResponseEntity.ok(ApiResponse.success(templateMapper.toResponse(value))))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Template not found")));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update template")
    ResponseEntity<ApiResponse<TemplateDto.Response>> updateTemplate(
            @PathVariable final Long id,
            @Valid @RequestBody final TemplateDto.UpdateRequest request) {
        log.info("Updating template with ID: {}", id);

        final UpdateTemplateCommand command = templateMapper.toUpdateCommand(id, request);
        final Template template = templateApplicationService.update(command);

        return ResponseEntity.ok(ApiResponse.success(templateMapper.toResponse(template), "Template updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete template")
    ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable final Long id) {
        log.info("Deleting template with ID: {}", id);

        templateApplicationService.delete(id);

        return ResponseEntity.ok(ApiResponse.success("Template deleted successfully"));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate template")
    ResponseEntity<ApiResponse<TemplateDto.Response>> activateTemplate(@PathVariable final Long id) {
        log.info("Activating template with ID: {}", id);

        final Template template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        template.activate();
        final Template savedTemplate = templateRepository.save(template);

        return ResponseEntity.ok(ApiResponse.success(templateMapper.toResponse(savedTemplate), "Template activated successfully"));
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archive template")
    ResponseEntity<ApiResponse<TemplateDto.Response>> archiveTemplate(@PathVariable final Long id) {
        log.info("Archiving template with ID: {}", id);

        final Template template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        template.deactivate();
        final Template savedTemplate = templateRepository.save(template);

        return ResponseEntity.ok(ApiResponse.success(templateMapper.toResponse(savedTemplate), "Template archived successfully"));
    }

    @PostMapping("/{id}/duplicate")
    @Operation(summary = "Duplicate template")
    ResponseEntity<ApiResponse<TemplateDto.Response>> duplicateTemplate(@PathVariable final Long id) {
        log.info("Duplicating template with ID: {}", id);

        final Template original = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        final Template duplicate = Template.builder()
                .name(original.getName() + " (Copy)")
                .subject(original.getSubject())
                .category(original.getCategory())
                .tags(original.getTags())
                .htmlContent(original.getHtmlContent())
                .textContent(original.getTextContent())
                .customFields(original.getCustomFields())
                .status(TemplateStatus.DRAFT)
                .thumbnailUrl(original.getThumbnailUrl())
                .isDefault(false)
                .usageCount(0)
                .campaignsCount(0)
                .build();

        final Template savedDuplicate = templateRepository.save(duplicate);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(templateMapper.toResponse(savedDuplicate), "Template duplicated successfully"));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get template statistics")
    ResponseEntity<ApiResponse<Map<String, Object>>> getTemplateStatistics() {
        log.info("Getting template statistics");

        long total = templateRepository.count();
        long draft = templateRepository.countByStatus(TemplateStatus.DRAFT);
        long active = templateRepository.countByStatus(TemplateStatus.ACTIVE);
        long archived = templateRepository.countByStatus(TemplateStatus.ARCHIVED);

        Map<String, Long> byCategory = new HashMap<>();
        for (TemplateCategory category : TemplateCategory.values()) {
            long count = templateRepository.countByCategory(category);
            byCategory.put(category.name().toLowerCase(), count);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("draft", draft);
        stats.put("active", active);
        stats.put("archived", archived);
        stats.put("byCategory", byCategory);

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get templates by category")
    ResponseEntity<ApiResponse<PagedResponse<TemplateDto.Response>>> getTemplatesByCategory(
            @PathVariable final TemplateCategory category,
            final Pageable pageable) {
        log.info("Getting templates by category: {}", category);

        final Page<Template> templatesPage = templateRepository.findByCategory(category, pageable);

        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(templatesPage, templateMapper::toResponse)));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active templates")
    ResponseEntity<ApiResponse<PagedResponse<TemplateDto.Response>>> getActiveTemplates(final Pageable pageable) {
        log.info("Getting all active templates");

        final Page<Template> templatesPage = templateRepository.findByStatus(TemplateStatus.ACTIVE, pageable);

        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(templatesPage, templateMapper::toResponse)));
    }
}
