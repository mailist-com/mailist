package com.mailist.mailist.auth.application.usecase.command;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class RemoveTeamMemberCommand {
    private Long requesterId; // User making the request
    private Long memberId; // User to be removed
}
