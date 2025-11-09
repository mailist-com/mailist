package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.usecase.command.UpdateTeamMemberRoleCommand;
import com.mailist.mailist.auth.application.usecase.dto.TeamMemberResult;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
final class UpdateTeamMemberRoleUseCase {

    private final UserRepository userRepository;

    TeamMemberResult execute(final UpdateTeamMemberRoleCommand command) {
        log.info("Updating role for member ID: {} by user ID: {}", command.getMemberId(), command.getRequesterId());

        // Verify requester exists and has permission
        final User requester = userRepository.findById(command.getRequesterId())
                .orElseThrow(() -> new IllegalArgumentException("Requester not found"));

        if (!requester.isOwner() && !requester.isAdmin()) {
            throw new IllegalArgumentException("Only owners and admins can update member roles");
        }

        // Find member to update
        final User member = userRepository.findByIdAndTenantId(command.getMemberId(), requester.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Team member not found"));

        // Cannot change owner role
        if (member.isOwner()) {
            throw new IllegalArgumentException("Cannot change the role of the owner");
        }

        // Parse and validate role
        User.Role newRole;
        try {
            newRole = User.Role.valueOf(command.getRole().toUpperCase());
            if (newRole == User.Role.OWNER) {
                throw new IllegalArgumentException("Cannot assign OWNER role");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + command.getRole());
        }

        // Update role
        member.setRoles(Set.of(newRole));
        final User updatedMember = userRepository.save(member);

        log.info("Successfully updated role for member ID: {} to: {}", member.getId(), newRole);

        return TeamMemberResult.builder()
                .user(updatedMember)
                .build();
    }
}
