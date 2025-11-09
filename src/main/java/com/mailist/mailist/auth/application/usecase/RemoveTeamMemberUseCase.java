package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.usecase.command.RemoveTeamMemberCommand;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
final class RemoveTeamMemberUseCase {

    private final UserRepository userRepository;

    void execute(final RemoveTeamMemberCommand command) {
        log.info("Removing member ID: {} by user ID: {}", command.getMemberId(), command.getRequesterId());

        // Verify requester exists and has permission
        final User requester = userRepository.findById(command.getRequesterId())
                .orElseThrow(() -> new IllegalArgumentException("Requester not found"));

        if (!requester.isOwner() && !requester.isAdmin()) {
            throw new IllegalArgumentException("Only owners and admins can remove team members");
        }

        // Find member to remove
        final User member = userRepository.findByIdAndTenantId(command.getMemberId(), requester.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Team member not found"));

        // Cannot remove owner
        if (member.isOwner()) {
            throw new IllegalArgumentException("Cannot remove the owner");
        }

        // Remove member
        userRepository.delete(member);

        log.info("Successfully removed member ID: {}", command.getMemberId());
    }
}
