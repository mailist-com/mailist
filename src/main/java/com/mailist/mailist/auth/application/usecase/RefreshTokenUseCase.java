package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.usecase.command.RefreshTokenCommand;
import com.mailist.mailist.auth.application.usecase.dto.RefreshTokenResult;
import com.mailist.mailist.auth.domain.aggregate.RefreshToken;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.domain.service.JwtService;
import com.mailist.mailist.auth.infrastructure.repository.RefreshTokenRepository;
import com.mailist.mailist.auth.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
final class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    RefreshTokenResult execute(final RefreshTokenCommand command) {
        log.info("Executing refresh token use case");

        // Find and validate refresh token
        final RefreshToken refreshToken = refreshTokenRepository.findByToken(command.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            log.warn("Refresh token is not valid (expired or revoked)");
            throw new IllegalArgumentException("Refresh token is expired or invalid");
        }

        // Get user
        final User user = refreshToken.getUser();

        if (user.getStatus() != User.Status.ACTIVE) {
            log.warn("User account is not active: {}", user.getEmail());
            throw new IllegalArgumentException("User account is not active");
        }

        // Generate new access token
        final String newAccessToken = jwtService.generateAccessToken(user);

        log.info("Successfully refreshed token for user: {}", user.getEmail());

        return RefreshTokenResult.builder()
                .token(newAccessToken)
                .build();
    }
}
