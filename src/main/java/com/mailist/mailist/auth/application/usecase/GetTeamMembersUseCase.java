package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.usecase.dto.TeamMembersListResult;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
final class GetTeamMembersUseCase {

    private final UserRepository userRepository;

    TeamMembersListResult execute(final Long userId) {
        log.info("Fetching team members for user ID: {}", userId);

        final User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        final Long tenantId = currentUser.getTenantId();
        final List<User> members = userRepository.findAllByTenantId(tenantId);

        final Integer totalMembers = members.size();
        final Integer activeMembers = userRepository.countActiveByTenantId(tenantId);
        final Integer pendingInvites = userRepository.countPendingByTenantId(tenantId);

        log.info("Found {} team members for tenant ID: {}", totalMembers, tenantId);

        return TeamMembersListResult.builder()
                .members(members)
                .totalMembers(totalMembers)
                .activeMembers(activeMembers)
                .pendingInvites(pendingInvites)
                .build();
    }
}
