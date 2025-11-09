package com.mailist.mailist.auth.interfaces.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberResponseDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String avatar;
    private String role; // OWNER, ADMIN, USER
    private String status; // ACTIVE, PENDING_VERIFICATION, INACTIVE, SUSPENDED
    private LocalDateTime joinedAt;
    private LocalDateTime lastLoginAt;
}
