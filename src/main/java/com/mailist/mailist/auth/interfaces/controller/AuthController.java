package com.mailist.mailist.auth.interfaces.controller;

import com.mailist.mailist.auth.application.usecase.*;
import com.mailist.mailist.auth.interfaces.dto.*;
import com.mailist.mailist.auth.application.usecase.*;
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

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Authentication", description = "User authentication and registration")
public class AuthController {
    
    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final Verify2FAUseCase verify2FAUseCase;
    private final AuthMapper authMapper;
    
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user and create their organization tenant")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequestDto registerDto) {
        log.info("Received registration request for email: {}", registerDto.getEmail());
        
        try {
            RegisterUserCommand command = authMapper.toCommand(registerDto);
            registerUserUseCase.execute(command);
            
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
    public ResponseEntity<ApiResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequestDto verifyDto) {
        log.info("Email verification attempt for: {}", verifyDto.getEmail());
        
        try {
            VerifyEmailCommand command = authMapper.toCommand(verifyDto);
            verifyEmailUseCase.execute(command);
            
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
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginDto) {
        log.info("Login attempt for: {}", loginDto.getEmail());
        
        try {
            LoginCommand command = authMapper.toCommand(loginDto);
            LoginUseCase.LoginResult result = loginUseCase.execute(command);
            
            LoginResponseDto response = authMapper.toLoginResponse(result);
            
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
    public ResponseEntity<ApiResponse> requestPasswordReset(@Valid @RequestBody RequestPasswordResetDto requestDto) {
        log.info("Password reset requested for: {}", requestDto.getEmail());
        
        try {
            RequestPasswordResetCommand command = authMapper.toCommand(requestDto);
            requestPasswordResetUseCase.execute(command);
            
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
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequestDto resetDto) {
        log.info("Password reset attempt for: {}", resetDto.getEmail());

        try {
            ResetPasswordCommand command = authMapper.toCommand(resetDto);
            resetPasswordUseCase.execute(command);

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

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Generate new access token using refresh token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDto refreshDto) {
        log.info("Refresh token request");

        try {
            RefreshTokenCommand command = authMapper.toCommand(refreshDto);
            RefreshTokenUseCase.RefreshTokenResult result = refreshTokenUseCase.execute(command);

            RefreshTokenResponseDto response = RefreshTokenResponseDto.success(result.getToken());

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
    public ResponseEntity<ApiResponse> logout(@RequestAttribute(value = "userId", required = false) Long userId) {
        log.info("Logout request for user ID: {}", userId);

        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Authentication required"));
            }

            logoutUseCase.execute(userId);

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

    @PostMapping("/verify-2fa")
    @Operation(summary = "Verify 2FA code", description = "Verify two-factor authentication code")
    public ResponseEntity<?> verify2FA(@Valid @RequestBody Verify2FARequestDto verify2FADto) {
        log.info("2FA verification request for user ID: {}", verify2FADto.getUserId());

        try {
            Verify2FACommand command = authMapper.toCommand(verify2FADto);
            Verify2FAUseCase.Verify2FAResult result = verify2FAUseCase.execute(command);

            Verify2FAResponseDto response = Verify2FAResponseDto.success(result.isVerified());

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