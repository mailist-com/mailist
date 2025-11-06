package com.mailist.mailist.contact.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

public class ContactDto {
    
    @Data
    @Builder
    @Schema(description = "Contact response")
    public static class Response {
        @Schema(description = "Contact ID", example = "1")
        private Long id;
        
        @Schema(description = "First name", example = "John")
        private String firstName;
        
        @Schema(description = "Last name", example = "Doe")
        private String lastName;
        
        @Schema(description = "Email address", example = "john.doe@example.com")
        private String email;
        
        @Schema(description = "Phone number", example = "+1234567890")
        private String phone;
        
        @Schema(description = "Lead score", example = "50")
        private Integer leadScore;
        
        @Schema(description = "Tags assigned to contact")
        private Set<TagDto> tags;
        
        @Schema(description = "Lists contact belongs to")
        private Set<ContactListDto> lists;
        
        @Schema(description = "Last activity timestamp")
        private LocalDateTime lastActivityAt;
        
        @Schema(description = "Creation timestamp")
        private LocalDateTime createdAt;
        
        @Schema(description = "Last update timestamp")
        private LocalDateTime updatedAt;
    }
    
    @Data
    @Builder
    @Schema(description = "Create contact request")
    public static class CreateRequest {
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        @Schema(description = "First name", example = "John", required = true)
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        @Schema(description = "Last name", example = "Doe", required = true)
        private String lastName;

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Schema(description = "Email address", example = "john.doe@example.com", required = true)
        private String email;

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        @Schema(description = "Phone number", example = "+1234567890")
        private String phone;

        @Schema(description = "List IDs to assign contact to")
        private Set<Long> listIds;
    }

    @Data
    @Builder
    @Schema(description = "Update contact request")
    public static class UpdateRequest {
        @Size(max = 100, message = "First name must not exceed 100 characters")
        @Schema(description = "First name", example = "John")
        private String firstName;

        @Size(max = 100, message = "Last name must not exceed 100 characters")
        @Schema(description = "Last name", example = "Doe")
        private String lastName;

        @Email(message = "Email must be valid")
        @Schema(description = "Email address", example = "john.doe@example.com")
        private String email;

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        @Schema(description = "Phone number", example = "+1234567890")
        private String phone;

        @Schema(description = "List IDs to assign contact to")
        private Set<Long> listIds;
    }

    @Data
    @Builder
    @Schema(description = "Add tag to contact request")
    public static class AddTagRequest {
        @NotBlank(message = "Tag name is required")
        @Size(max = 50, message = "Tag name must not exceed 50 characters")
        @Schema(description = "Tag name", example = "VIP", required = true)
        private String tagName;
        
        @Schema(description = "Tag color", example = "#FF5733")
        private String tagColor;
        
        @Schema(description = "Tag description", example = "Very Important Person")
        private String tagDescription;
    }
    
    @Data
    @Builder
    @Schema(description = "Tag information")
    public static class TagDto {
        @Schema(description = "Tag name", example = "VIP")
        private String name;
        
        @Schema(description = "Tag color", example = "#FF5733")
        private String color;
        
        @Schema(description = "Tag description", example = "Very Important Person")
        private String description;
    }
    
    @Data
    @Builder
    @Schema(description = "Contact list information")
    public static class ContactListDto {
        @Schema(description = "List ID", example = "1")
        private Long id;
        
        @Schema(description = "List name", example = "Newsletter Subscribers")
        private String name;
        
        @Schema(description = "List description", example = "Main newsletter subscriber list")
        private String description;
        
        @Schema(description = "Is dynamic list", example = "false")
        private Boolean isDynamic;
    }
}