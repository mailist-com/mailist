package com.mailist.mailist.auth.interfaces.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamMembersListResponseDto {

    private List<TeamMemberResponseDto> members;
    private Integer totalMembers;
    private Integer activeMembers;
    private Integer pendingInvites;
}
