package com.mailist.mailist.auth.application.usecase.command;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UpdateTeamMemberRoleCommand {
    private Long requesterId; // User making the request
    private Long memberId; // User whose role is being updated
    private String role; // ADMIN or USER
}
