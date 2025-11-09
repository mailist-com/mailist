package com.mailist.mailist.automation.interfaces.dto;

import com.mailist.mailist.automation.domain.valueobject.TriggerType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AutomationRuleDto {
    
    @Value
    @Builder
    @Schema(description = "Automation rule response")
    public static class Response {
        @Schema(description = "Rule ID", example = "1")
        Long id;

        @Schema(description = "Rule name", example = "Welcome Email Sequence")
        String name;

        @Schema(description = "Rule description", example = "Send welcome email when user joins newsletter")
        String description;

        @Schema(description = "Trigger type", example = "CONTACT_TAGGED")
        TriggerType triggerType;

        @Schema(description = "Whether rule is active", example = "true")
        Boolean isActive;

        @Schema(description = "Flow JSON definition with all nodes and connections")
        String flowJson;

        @Schema(description = "Creation timestamp")
        LocalDateTime createdAt;

        @Schema(description = "Last update timestamp")
        LocalDateTime updatedAt;
    }
    
    @Value
    @Builder
    @Schema(description = "Create automation rule request")
    public static class CreateRequest {
        @NotBlank(message = "Name is required")
        @Schema(description = "Rule name", example = "Welcome Email Sequence", required = true)
        String name;
        
        @Schema(description = "Rule description", example = "Send welcome email when user joins newsletter")
        String description;
        
        @NotNull(message = "Trigger type is required")
        @Schema(description = "Trigger type", example = "CONTACT_TAGGED", required = true)
        TriggerType triggerType;
        
        @Schema(description = "Whether rule is active", example = "true")
        Boolean isActive;

        @Schema(description = "Flow JSON definition with all nodes and connections")
        String flowJson;
    }
    
    @Value
    @Builder
    @Schema(description = "Update automation rule request")
    public static class UpdateRequest {
        @Schema(description = "Rule name", example = "Welcome Email Sequence")
        String name;

        @Schema(description = "Rule description", example = "Send welcome email when user joins newsletter")
        String description;

        @Schema(description = "Whether rule is active", example = "true")
        Boolean isActive;

        @Schema(description = "Flow JSON definition with all nodes and connections")
        String flowJson;
    }
}