package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.usecase.command.InviteTeamMemberCommand;
import com.mailist.mailist.auth.application.usecase.command.RemoveTeamMemberCommand;
import com.mailist.mailist.auth.application.usecase.command.UpdateTeamMemberRoleCommand;
import com.mailist.mailist.auth.application.usecase.dto.TeamMemberResult;
import com.mailist.mailist.auth.application.usecase.dto.TeamMembersListResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamApplicationService {

    private final GetTeamMembersUseCase getTeamMembersUseCase;
    private final InviteTeamMemberUseCase inviteTeamMemberUseCase;
    private final UpdateTeamMemberRoleUseCase updateTeamMemberRoleUseCase;
    private final RemoveTeamMemberUseCase removeTeamMemberUseCase;

    public TeamMembersListResult getTeamMembers(final Long userId) {
        return getTeamMembersUseCase.execute(userId);
    }

    public TeamMemberResult inviteTeamMember(final InviteTeamMemberCommand command) {
        return inviteTeamMemberUseCase.execute(command);
    }

    public TeamMemberResult updateTeamMemberRole(final UpdateTeamMemberRoleCommand command) {
        return updateTeamMemberRoleUseCase.execute(command);
    }

    public void removeTeamMember(final RemoveTeamMemberCommand command) {
        removeTeamMemberUseCase.execute(command);
    }
}
