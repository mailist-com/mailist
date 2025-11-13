package com.mailist.mailist.externalapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request DTO for creating/updating contacts via external API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalContactRequest {

    @NotBlank(message = "First name is required")
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @JsonProperty("last_name")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    private Set<String> tags;

    @JsonProperty("list_id")
    private Long listId;

    @JsonProperty("list_name")
    private String listName;
}
