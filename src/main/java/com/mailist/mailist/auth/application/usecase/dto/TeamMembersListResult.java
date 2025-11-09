package com.mailist.mailist.auth.application.usecase.dto;

import com.mailist.mailist.auth.domain.aggregate.User;
import lombok.Builder;

import java.util.List;

@Builder
public record TeamMembersListResult(
    List<User> members,
    Integer totalMembers,
    Integer activeMembers,
    Integer pendingInvites
) {
}
