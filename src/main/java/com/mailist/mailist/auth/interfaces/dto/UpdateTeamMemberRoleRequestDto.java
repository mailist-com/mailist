package com.mailist.mailist.auth.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTeamMemberRoleRequestDto {

    @NotNull(message = "Role is required")
    private String role; // ADMIN, USER
}
