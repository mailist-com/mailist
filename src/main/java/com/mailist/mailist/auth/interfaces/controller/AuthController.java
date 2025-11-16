package com.mailist.mailist.auth.interfaces.controller;

import com.mailist.mailist.auth.application.usecase.AuthApplicationService;
import com.mailist.mailist.auth.application.usecase.command.*;
import com.mailist.mailist.auth.application.usecase.dto.LoginResult;
import com.mailist.mailist.auth.application.usecase.dto.RefreshTokenResult;
import com.mailist.mailist.auth.application.usecase.dto.Verify2FAResult;
import com.mailist.mailist.auth.interfaces.dto.*;
import com.mailist.mailist.auth.interfaces.mapper.AuthMapper;
import com.mailist.mailist.shared.interfaces.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and registration")
class AuthController {
    

    private final AuthApplicationService authApplicationService;
    private final AuthMapper authMapper;
    
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user and create their organization tenant")
    ResponseEntity<ApiResponse> register(@Valid @RequestBody final RegisterRequestDto registerDto) {
        log.info("Received registration request for email: {}", registerDto.getEmail());
        
        try {
            final RegisterUserCommand command = authMapper.toCommand(registerDto);
            authApplicationService.register(command);
            
            log.info("Successfully registered user: {}", registerDto.getEmail());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Registration successful. Please check your email for verification code."));
            
        } catch (IllegalArgumentException e) {
            log.warn("Registration failed for email {}: {}", registerDto.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during registration for email: {}", registerDto.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Registration failed. Please try again."));
        }
    }
    
    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email with verification code")
    ResponseEntity<ApiResponse> verifyEmail(@Valid @RequestBody final VerifyEmailRequestDto verifyDto) {
        log.info("Email verification attempt for: {}", verifyDto.getEmail());
        
        try {
            final VerifyEmailCommand command = authMapper.toCommand(verifyDto);
            authApplicationService.verifyEmail(command);
            
            log.info("Email verified successfully for: {}", verifyDto.getEmail());
            
            return ResponseEntity.ok(ApiResponse.success("Email verified successfully. You can now login."));
            
        } catch (IllegalArgumentException e) {
            log.warn("Email verification failed for {}: {}", verifyDto.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during email verification for: {}", verifyDto.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Email verification failed. Please try again."));
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT tokens")
    ResponseEntity<?> login(@Valid @RequestBody final LoginRequestDto loginDto) {
        log.info("Login attempt for: {}", loginDto.getEmail());
        
        try {
            final LoginCommand command = authMapper.toCommand(loginDto);
            final LoginResult result = authApplicationService.login(command);
            
            final LoginResponseDto response = authMapper.toLoginResponse(result);
            
            log.info("Successful login for: {}", loginDto.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Login failed for {}: {}", loginDto.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during login for: {}", loginDto.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Login failed. Please try again."));
        }
    }
    
    @PostMapping("/request-password-reset")
    @Operation(summary = "Request password reset", description = "Send password reset code to user email")
    ResponseEntity<ApiResponse> requestPasswordReset(@Valid @RequestBody final RequestPasswordResetDto requestDto) {
        log.info("Password reset requested for: {}", requestDto.getEmail());
        
        try {
            final RequestPasswordResetCommand command = authMapper.toCommand(requestDto);
            authApplicationService.requestPasswordReset(command);
            
            // Always return success for security (don't reveal if email exists)
            return ResponseEntity.ok(ApiResponse.success("If the email exists, a reset code has been sent."));
            
        } catch (Exception e) {
            log.error("Unexpected error during password reset request for: {}", requestDto.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Password reset request failed. Please try again."));
        }
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset password", description = "Reset user password with reset code")
    ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody final ResetPasswordRequestDto resetDto) {
        log.info("Password reset attempt for: {}", resetDto.getEmail());

        try {
            final ResetPasswordCommand command = authMapper.toCommand(resetDto);
            authApplicationService.resetPassword(command);

            log.info("Password reset successfully for: {}", resetDto.getEmail());

            return ResponseEntity.ok(ApiResponse.success("Password reset successfully. You can now login with your new password."));

        } catch (IllegalArgumentException e) {
            log.warn("Password reset failed for {}: {}", resetDto.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during password reset for: {}", resetDto.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Password reset failed. Please try again."));
        }
    }

    @PostMapping("/set-password")
    @Operation(summary = "Set password", description = "Set password for invited team member using verification token")
    ResponseEntity<ApiResponse> setPassword(@Valid @RequestBody final SetPasswordRequestDto setPasswordDto) {
        log.info("Set password attempt with token");

        try {
            final SetPasswordCommand command = authMapper.toCommand(setPasswordDto);
            authApplicationService.setPassword(command);

            log.info("Password set successfully");

            return ResponseEntity.ok(ApiResponse.success("Password set successfully. You can now login."));

        } catch (IllegalArgumentException e) {
            log.warn("Set password failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during set password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to set password. Please try again."));
        }
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    ResponseEntity<?> refreshToken(@Valid @RequestBody final RefreshTokenRequestDto refreshDto) {
        log.info("Refresh token request");

        try {
            final RefreshTokenCommand command = authMapper.toCommand(refreshDto);
            final RefreshTokenResult result = authApplicationService.refreshToken(command);

            final RefreshTokenResponseDto response = RefreshTokenResponseDto.success(result.token());

            log.info("Successfully refreshed token");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during token refresh", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Token refresh failed. Please try again."));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Revoke all refresh tokens for the authenticated user")
    ResponseEntity<ApiResponse> logout(@RequestAttribute(value = "userId", required = false) final Long userId) {
        log.info("Logout request for user ID: {}", userId);

        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Authentication required"));
            }

            authApplicationService.logout(userId);

            log.info("Successfully logged out user ID: {}", userId);

            return ResponseEntity.ok(ApiResponse.success("Logout successful"));

        } catch (IllegalArgumentException e) {
            log.warn("Logout failed for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during logout for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Logout failed. Please try again."));
        }
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change password for authenticated user")
    ResponseEntity<ApiResponse> changePassword(
            @RequestAttribute(value = "userId", required = false) final Long userId,
            @Valid @RequestBody final ChangePasswordRequestDto changePasswordDto) {
        log.info("Change password request for user ID: {}", userId);

        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Authentication required"));
            }

            final ChangePasswordCommand command = authMapper.toCommand(changePasswordDto);
            command.setUserId(userId);
            authApplicationService.changePassword(command);

            log.info("Password changed successfully for user ID: {}", userId);

            return ResponseEntity.ok(ApiResponse.success("Password changed successfully. You have been logged out from all devices."));

        } catch (IllegalArgumentException e) {
            log.warn("Change password failed for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during password change for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Password change failed. Please try again."));
        }
    }

    @PostMapping("/verify-2fa")
    @Operation(summary = "Verify 2FA code", description = "Verify two-factor authentication code")
    ResponseEntity<?> verify2FA(@Valid @RequestBody final Verify2FARequestDto verify2FADto) {
        log.info("2FA verification request for user ID: {}", verify2FADto.getUserId());

        try {
            final Verify2FACommand command = authMapper.toCommand(verify2FADto);
            final Verify2FAResult result = authApplicationService.verify2FA(command);

            final Verify2FAResponseDto response = Verify2FAResponseDto.success(result.verified());

            log.info("2FA verification successful for user ID: {}", verify2FADto.getUserId());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("2FA verification failed for user ID {}: {}", verify2FADto.getUserId(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during 2FA verification for user ID: {}", verify2FADto.getUserId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("2FA verification failed. Please try again."));
        }
    }
}