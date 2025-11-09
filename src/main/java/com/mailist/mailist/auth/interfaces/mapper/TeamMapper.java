package com.mailist.mailist.auth.interfaces.mapper;

import com.mailist.mailist.auth.application.usecase.command.InviteTeamMemberCommand;
import com.mailist.mailist.auth.application.usecase.command.UpdateTeamMemberRoleCommand;
import com.mailist.mailist.auth.application.usecase.dto.TeamMemberResult;
import com.mailist.mailist.auth.application.usecase.dto.TeamMembersListResult;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.interfaces.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TeamMapper {

    // Command mappings
    @Mapping(target = "inviterId", ignore = true)
    InviteTeamMemberCommand toCommand(InviteTeamMemberRequestDto dto);

    @Mapping(target = "requesterId", ignore = true)
    @Mapping(target = "memberId", ignore = true)
    UpdateTeamMemberRoleCommand toRoleCommand(UpdateTeamMemberRoleRequestDto dto);

    // Response mappings
    default TeamMemberResponseDto toMemberResponse(TeamMemberResult result) {
        User user = result.user();
        return toMemberResponse(user);
    }

    default TeamMemberResponseDto toMemberResponse(User user) {
        return TeamMemberResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .avatar(user.getAvatar())
                .role(getPrimaryRole(user.getRoles()))
                .status(user.getStatus() != null ? user.getStatus().name() : "PENDING_VERIFICATION")
                .joinedAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    default TeamMembersListResponseDto toListResponse(TeamMembersListResult result) {
        List<TeamMemberResponseDto> members = result.members().stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());

        return TeamMembersListResponseDto.builder()
                .members(members)
                .totalMembers(result.totalMembers())
                .activeMembers(result.activeMembers())
                .pendingInvites(result.pendingInvites())
                .build();
    }

    default String getPrimaryRole(Set<User.Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return "USER";
        }

        // Priority order: OWNER > ADMIN > USER
        if (roles.contains(User.Role.OWNER)) {
            return "OWNER";
        }
        if (roles.contains(User.Role.ADMIN)) {
            return "ADMIN";
        }
        return "USER";
    }
}
