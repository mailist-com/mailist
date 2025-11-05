package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.port.out.RefreshTokenRepository;
import com.mailist.mailist.auth.application.port.out.UserRepository;
import com.mailist.mailist.auth.domain.aggregate.RefreshToken;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.domain.service.JwtService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public RefreshTokenResult execute(RefreshTokenCommand command) {
        log.info("Executing refresh token use case");

        // Find and validate refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(command.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            log.warn("Refresh token is not valid (expired or revoked)");
            throw new IllegalArgumentException("Refresh token is expired or invalid");
        }

        // Get user
        User user = refreshToken.getUser();

        if (user.getStatus() != User.Status.ACTIVE) {
            log.warn("User account is not active: {}", user.getEmail());
            throw new IllegalArgumentException("User account is not active");
        }

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);

        log.info("Successfully refreshed token for user: {}", user.getEmail());

        return RefreshTokenResult.builder()
                .token(newAccessToken)
                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class RefreshTokenResult {
        private String token;
    }
}
