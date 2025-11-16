package com.mailist.mailist.template.interfaces.dto;

import com.mailist.mailist.template.domain.valueobject.CustomField;
import com.mailist.mailist.template.domain.valueobject.TemplateCategory;
import com.mailist.mailist.template.domain.valueobject.TemplateStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class TemplateDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Template response")
    public static class Response {
        @Schema(description = "Template ID", example = "1")
        private Long id;

        @Schema(description = "Template name", example = "Welcome Email")
        private String name;

        @Schema(description = "Email subject", example = "Welcome to our platform!")
        private String subject;

        @Schema(description = "Template category")
        private TemplateCategory category;

        @Schema(description = "Template tags")
        private Set<String> tags;

        @Schema(description = "Template content")
        private TemplateContentDto content;

        @Schema(description = "Custom fields")
        private List<CustomField> customFields;

        @Schema(description = "Template status")
        private TemplateStatus status;

        @Schema(description = "Thumbnail URL")
        private String thumbnailUrl;

        @Schema(description = "Template statistics")
        private TemplateStatisticsDto statistics;

        @Schema(description = "Creation timestamp")
        private LocalDateTime createdAt;

        @Schema(description = "Last update timestamp")
        private LocalDateTime updatedAt;

        @Schema(description = "Creator identifier")
        private String createdBy;

        @Schema(description = "Is default template")
        private Boolean isDefault;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Create template request")
    public static class CreateRequest {
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        @Schema(description = "Template name", example = "Welcome Email", required = true)
        private String name;

        @NotBlank(message = "Subject is required")
        @Size(max = 500, message = "Subject must not exceed 500 characters")
        @Schema(description = "Email subject", example = "Welcome to our platform!", required = true)
        private String subject;

        @NotNull(message = "Category is required")
        @Schema(description = "Template category", required = true)
        private TemplateCategory category;

        @Schema(description = "Template tags")
        private Set<String> tags;

        @NotNull(message = "Content is required")
        @Schema(description = "Template content", required = true)
        private TemplateContentDto content;

        @Schema(description = "Custom fields for template variables")
        private List<CustomField> customFields;

        @Schema(description = "Template status (default: DRAFT)")
        private TemplateStatus status;

        @Size(max = 500, message = "Thumbnail URL must not exceed 500 characters")
        @Schema(description = "Thumbnail URL")
        private String thumbnailUrl;

        @Schema(description = "Creator identifier")
        private String createdBy;

        @Schema(description = "Is default template")
        private Boolean isDefault;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update template request")
    public static class UpdateRequest {
        @Size(max = 255, message = "Name must not exceed 255 characters")
        @Schema(description = "Template name")
        private String name;

        @Size(max = 500, message = "Subject must not exceed 500 characters")
        @Schema(description = "Email subject")
        private String subject;

        @Schema(description = "Template category")
        private TemplateCategory category;

        @Schema(description = "Template tags")
        private Set<String> tags;

        @Schema(description = "Template content")
        private TemplateContentDto content;

        @Schema(description = "Custom fields for template variables")
        private List<CustomField> customFields;

        @Schema(description = "Template status")
        private TemplateStatus status;

        @Size(max = 500, message = "Thumbnail URL must not exceed 500 characters")
        @Schema(description = "Thumbnail URL")
        private String thumbnailUrl;

        @Schema(description = "Is default template")
        private Boolean isDefault;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Template content")
    public static class TemplateContentDto {
        @NotBlank(message = "HTML content is required")
        @Schema(description = "HTML content", required = true)
        private String html;

        @Schema(description = "Plain text content")
        private String text;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Template statistics")
    public static class TemplateStatisticsDto {
        @Schema(description = "Usage count", example = "42")
        private Integer usageCount;

        @Schema(description = "Last used timestamp")
        private LocalDateTime lastUsedAt;

        @Schema(description = "Campaigns count", example = "15")
        private Integer campaignsCount;

        @Schema(description = "Average open rate", example = "0.45")
        private Double avgOpenRate;

        @Schema(description = "Average click rate", example = "0.12")
        private Double avgClickRate;
    }
}
