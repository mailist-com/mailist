package com.mailist.mailist.externalapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Response DTO for contact operations via external API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalContactResponse {

    private Long id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String email;

    private String phone;

    private Set<String> tags;

    @JsonProperty("lead_score")
    private Integer leadScore;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public static ExternalContactResponse fromEntity(Contact contact) {
        return ExternalContactResponse.builder()
                .id(contact.getId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .tags(contact.getTags().stream()
                        .map(tag -> tag.getName())
                        .collect(Collectors.toSet()))
                .leadScore(contact.getLeadScore())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }
}
