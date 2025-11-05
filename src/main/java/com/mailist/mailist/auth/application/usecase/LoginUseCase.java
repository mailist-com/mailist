package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.port.out.RefreshTokenRepository;
import com.mailist.mailist.auth.application.port.out.UserRepository;
import com.mailist.mailist.auth.domain.aggregate.RefreshToken;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.domain.service.JwtService;
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoginUseCase {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public LoginResult execute(LoginCommand command) {
        log.info("Login attempt for email: {}", command.getEmail());
        
        // Find user by email
        User user = userRepository.findByEmail(command.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        
        // Check if account is locked
        if (user.isAccountLocked()) {
            log.warn("Account locked for user: {}", command.getEmail());
            throw new IllegalArgumentException("Account is temporarily locked due to too many failed attempts");
        }
        
        // Verify password
        if (!passwordEncoder.matches(command.getPassword(), user.getPassword())) {
            user.incrementFailedLoginAttempts();
            userRepository.save(user);
            log.warn("Invalid password for user: {}", command.getEmail());
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        // Check if email is verified
        if (!user.getEmailVerified()) {
            log.warn("Email not verified for user: {}", command.getEmail());
            throw new IllegalArgumentException("Please verify your email before logging in");
        }
        
        // Check if user is active
        if (user.getStatus() != User.Status.ACTIVE) {
            log.warn("User account not active: {} - Status: {}", command.getEmail(), user.getStatus());
            throw new IllegalArgumentException("Account is not active");
        }
        
        // Set tenant context
        TenantContext.setOrganizationId(user.getTenantId());
        
        try {
            // Update last login and reset failed attempts
            user.updateLastLogin();
            userRepository.save(user);
            
            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshTokenValue = jwtService.generateRefreshToken(user);
            
            // Save refresh token
            RefreshToken refreshToken = RefreshToken.builder()
                    .token(refreshTokenValue)
                    .user(user)
                    .expiresAt(jwtService.getRefreshTokenExpiration())
                    .build();
            
            refreshTokenRepository.save(refreshToken);
            
            log.info("Successful login for user: {}", command.getEmail());
            
            return LoginResult.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshTokenValue)
                    .user(user)
                    .expiresAt(jwtService.getAccessTokenExpiration())
                    .build();
                    
        } finally {
            TenantContext.clear();
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class LoginResult {
        private String accessToken;
        private String refreshToken;
        private User user;
        private java.time.LocalDateTime expiresAt;
    }
}