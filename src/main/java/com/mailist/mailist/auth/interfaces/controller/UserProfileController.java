package com.mailist.mailist.auth.interfaces.controller;

import com.mailist.mailist.auth.application.usecase.UserProfileApplicationService;
import com.mailist.mailist.auth.application.usecase.command.UpdateNotificationSettingsCommand;
import com.mailist.mailist.auth.application.usecase.command.UpdatePreferencesCommand;
import com.mailist.mailist.auth.application.usecase.command.UpdateProfileCommand;
import com.mailist.mailist.auth.application.usecase.dto.UserProfileResult;
import com.mailist.mailist.auth.interfaces.dto.*;
import com.mailist.mailist.auth.interfaces.mapper.UserProfileMapper;
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
@RequestMapping("/api/v1/profile")
@Tag(name = "User Profile", description = "User profile management")
@SecurityRequirement(name = "bearer-jwt")
class UserProfileController {

    private final UserProfileApplicationService userProfileApplicationService;
    private final UserProfileMapper userProfileMapper;

    @GetMapping
    @Operation(summary = "Get current user profile", description = "Retrieve the profile of the authenticated user")
    ResponseEntity<?> getCurrentUserProfile(@RequestAttribute(value = "userId", required = false) final Long userId) {
        log.info("Fetching profile for user ID: {}", userId);

        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Authentication required"));
            }

            final UserProfileResult result = userProfileApplicationService.getUserProfile(userId);
            final UserProfileResponseDto response = userProfileMapper.toResponse(result);

            log.info("Successfully retrieved profile for user ID: {}", userId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Failed to fetch profile for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching profile for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch profile"));
        }
    }

    @PutMapping
    @Operation(summary = "Update user profile", description = "Update basic profile information")
    ResponseEntity<?> updateProfile(
            @RequestAttribute(value = "userId", required = false) final Long userId,
            @Valid @RequestBody final UpdateProfileRequestDto updateDto) {
        log.info("Updating profile for user ID: {}", userId);

        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Authentication required"));
            }

            final UpdateProfileCommand command = userProfileMapper.toCommand(updateDto);
            command.setUserId(userId);

            final UserProfileResult result = userProfileApplicationService.updateProfile(command);
            final UserProfileResponseDto response = userProfileMapper.toResponse(result);

            log.info("Successfully updated profile for user ID: {}", userId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Failed to update profile for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating profile for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update profile"));
        }
    }

    @PutMapping("/notifications")
    @Operation(summary = "Update notification settings", description = "Update user notification preferences")
    ResponseEntity<?> updateNotificationSettings(
            @RequestAttribute(value = "userId", required = false) final Long userId,
            @Valid @RequestBody final UpdateNotificationSettingsRequestDto notificationDto) {
        log.info("Updating notification settings for user ID: {}", userId);

        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Authentication required"));
            }

            final UpdateNotificationSettingsCommand command = userProfileMapper.toNotificationCommand(notificationDto);
            command.setUserId(userId);

            final UserProfileResult result = userProfileApplicationService.updateNotificationSettings(command);
            final UserProfileResponseDto response = userProfileMapper.toResponse(result);

            log.info("Successfully updated notification settings for user ID: {}", userId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Failed to update notification settings for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating notification settings for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update notification settings"));
        }
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update user preferences", description = "Update user email and display preferences")
    ResponseEntity<?> updatePreferences(
            @RequestAttribute(value = "userId", required = false) final Long userId,
            @Valid @RequestBody final UpdatePreferencesRequestDto preferencesDto) {
        log.info("Updating preferences for user ID: {}", userId);

        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Authentication required"));
            }

            final UpdatePreferencesCommand command = userProfileMapper.toPreferencesCommand(preferencesDto);
            command.setUserId(userId);

            final UserProfileResult result = userProfileApplicationService.updatePreferences(command);
            final UserProfileResponseDto response = userProfileMapper.toResponse(result);

            log.info("Successfully updated preferences for user ID: {}", userId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Failed to update preferences for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating preferences for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update preferences"));
        }
    }
}
