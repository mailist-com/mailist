package com.mailist.mailist.automation.interfaces.dto;

import com.mailist.mailist.automation.domain.valueobject.Action;
import com.mailist.mailist.automation.domain.valueobject.ConditionType;
import com.mailist.mailist.automation.domain.valueobject.TriggerType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

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
        
        @Schema(description = "Rule conditions")
        List<ConditionDto.Response> conditions;
        
        @Schema(description = "Rule actions")
        List<ActionDto.Response> actions;
        
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
        
        @NotEmpty(message = "At least one condition is required")
        @Valid
        @Schema(description = "Rule conditions", required = true)
        List<ConditionDto.CreateRequest> conditions;
        
        @NotEmpty(message = "At least one action is required")
        @Valid
        @Schema(description = "Rule actions", required = true)
        List<ActionDto.CreateRequest> actions;
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
        
        @Valid
        @Schema(description = "Rule conditions")
        List<ConditionDto.UpdateRequest> conditions;
        
        @Valid
        @Schema(description = "Rule actions")
        List<ActionDto.UpdateRequest> actions;
    }
    
    public static class ConditionDto {
        
        @Value
        @Builder
        @Schema(description = "Condition response")
        public static class Response {
            @Schema(description = "Condition type", example = "HAS_TAG")
            ConditionType type;
            
            @Schema(description = "Condition field", example = "tag")
            String field;
            
            @Schema(description = "Condition operator", example = "EQUALS")
            String operator;
            
            @Schema(description = "Condition value", example = "newsletter")
            String value;
        }
        
        @Value
        @Builder
        @Schema(description = "Create condition request")
        public static class CreateRequest {
            @NotNull(message = "Condition type is required")
            @Schema(description = "Condition type", example = "HAS_TAG", required = true)
            ConditionType type;
            
            @NotBlank(message = "Field is required")
            @Schema(description = "Condition field", example = "tag", required = true)
            String field;
            
            @NotBlank(message = "Operator is required")
            @Schema(description = "Condition operator", example = "EQUALS", required = true)
            String operator;
            
            @NotBlank(message = "Value is required")
            @Schema(description = "Condition value", example = "newsletter", required = true)
            String value;
        }
        
        @Value
        @Builder
        @Schema(description = "Update condition request")
        public static class UpdateRequest {
            @Schema(description = "Condition type", example = "HAS_TAG")
            ConditionType type;
            
            @Schema(description = "Condition field", example = "tag")
            String field;
            
            @Schema(description = "Condition operator", example = "EQUALS")
            String operator;
            
            @Schema(description = "Condition value", example = "newsletter")
            String value;
        }
    }
    
    public static class ActionDto {
        
        @Value
        @Builder
        @Schema(description = "Action response")
        public static class Response {
            @Schema(description = "Action type", example = "SEND_EMAIL")
            Action.ActionType type;
            
            @Schema(description = "Action target", example = "campaign_id")
            String target;
            
            @Schema(description = "Action parameters", example = "{\"template_id\": \"123\"}")
            String parameters;
        }
        
        @Value
        @Builder
        @Schema(description = "Create action request")
        public static class CreateRequest {
            @NotNull(message = "Action type is required")
            @Schema(description = "Action type", example = "SEND_EMAIL", required = true)
            Action.ActionType type;
            
            @NotBlank(message = "Target is required")
            @Schema(description = "Action target", example = "campaign_id", required = true)
            String target;
            
            @Schema(description = "Action parameters", example = "{\"template_id\": \"123\"}")
            String parameters;
        }
        
        @Value
        @Builder
        @Schema(description = "Update action request")
        public static class UpdateRequest {
            @Schema(description = "Action type", example = "SEND_EMAIL")
            Action.ActionType type;
            
            @Schema(description = "Action target", example = "campaign_id")
            String target;
            
            @Schema(description = "Action parameters", example = "{\"template_id\": \"123\"}")
            String parameters;
        }
    }
}