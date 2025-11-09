package com.mailist.mailist.auth.interfaces.controller;

import com.mailist.mailist.auth.application.usecase.TeamApplicationService;
import com.mailist.mailist.auth.application.usecase.command.InviteTeamMemberCommand;
import com.mailist.mailist.auth.application.usecase.command.RemoveTeamMemberCommand;
import com.mailist.mailist.auth.application.usecase.command.UpdateTeamMemberRoleCommand;
import com.mailist.mailist.auth.application.usecase.dto.TeamMemberResult;
import com.mailist.mailist.auth.application.usecase.dto.TeamMembersListResult;
import com.mailist.mailist.auth.interfaces.dto.InviteTeamMemberRequestDto;
import com.mailist.mailist.auth.interfaces.dto.TeamMemberResponseDto;
import com.mailist.mailist.auth.interfaces.dto.TeamMembersListResponseDto;
import com.mailist.mailist.auth.interfaces.dto.UpdateTeamMemberRoleRequestDto;
import com.mailist.mailist.auth.interfaces.mapper.TeamMapper;
import com.mailist.mailist.shared.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/team")
@Tag(name = "Team Management", description = "Team member management operations")
@SecurityRequirement(name = "bearer-jwt")
class TeamController {

    private final TeamApplicationService teamApplicationService;
    private final TeamMapper teamMapper;

    @GetMapping("/members")
    @Operation(summary = "Get team members", description = "Retrieve all team members for the current organization")
    ResponseEntity<?> getTeamMembers(@RequestAttribute(value = "userId", required = false) final Long userId) {
        log.info("Fetching team members for user ID: {}", userId);

        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Authentication required"));
            }

            final TeamMembersListResult result = teamApplicationService.getTeamMembers(userId);
            final TeamMembersListResponseDto response = teamMapper.toListResponse(result);

            log.info("Successfully retrieved {} team members for user ID: {}", response.getTotalMembers(), userId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Failed to fetch team members for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching team members for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch team members"));
        }
    }

    @PostMapping("/members/invite")
    @Operation(summary = "Invite team member", description = "Invite a new member to join the team")
    ResponseEntity<?> inviteTeamMember(
            @RequestAttribute(value = "userId", required = false) final Long userId,
            @Valid @RequestBody final InviteTeamMemberRequestDto inviteDto) {
        log.info("Inviting team member: {} by user ID: {}", inviteDto.getEmail(), userId);

        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Authentication required"));
            }

            final InviteTeamMemberCommand command = teamMapper.toCommand(inviteDto);
            command.setInviterId(userId);

            final TeamMemberResult result = teamApplicationService.inviteTeamMember(command);
            final TeamMemberResponseDto response = teamMapper.toMemberResponse(result);

            log.info("Successfully invited team member: {} with ID: {}", inviteDto.getEmail(), response.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Failed to invite team member for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error inviting team member for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to invite team member"));
        }
    }

    @PutMapping("/members/{memberId}/role")
    @Operation(summary = "Update member role", description = "Update the role of a team member")
    ResponseEntity<?> updateMemberRole(
            @RequestAttribute(value = "userId", required = false) final Long userId,
            @PathVariable final Long memberId,
            @Valid @RequestBody final UpdateTeamMemberRoleRequestDto roleDto) {
        log.info("Updating role for member ID: {} by user ID: {}", memberId, userId);

        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Authentication required"));
            }

            final UpdateTeamMemberRoleCommand command = teamMapper.toRoleCommand(roleDto);
            command.setRequesterId(userId);
            command.setMemberId(memberId);

            final TeamMemberResult result = teamApplicationService.updateTeamMemberRole(command);
            final TeamMemberResponseDto response = teamMapper.toMemberResponse(result);

            log.info("Successfully updated role for member ID: {} to: {}", memberId, roleDto.getRole());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Failed to update member role for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating member role for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update member role"));
        }
    }

    @DeleteMapping("/members/{memberId}")
    @Operation(summary = "Remove team member", description = "Remove a member from the team")
    ResponseEntity<?> removeMember(
            @RequestAttribute(value = "userId", required = false) final Long userId,
            @PathVariable final Long memberId) {
        log.info("Removing member ID: {} by user ID: {}", memberId, userId);

        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Authentication required"));
            }

            final RemoveTeamMemberCommand command = RemoveTeamMemberCommand.builder()
                    .requesterId(userId)
                    .memberId(memberId)
                    .build();

            teamApplicationService.removeTeamMember(command);

            log.info("Successfully removed member ID: {}", memberId);

            return ResponseEntity.ok(ApiResponse.success("Team member removed successfully"));

        } catch (IllegalArgumentException e) {
            log.warn("Failed to remove member for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error removing member for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to remove team member"));
        }
    }
}
