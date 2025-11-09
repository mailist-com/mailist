package com.mailist.mailist.auth.application.usecase.command;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class InviteTeamMemberCommand {
    private Long inviterId; // User who is inviting
    private String email;
    private String firstName;
    private String lastName;
    private String role; // ADMIN or USER
}
