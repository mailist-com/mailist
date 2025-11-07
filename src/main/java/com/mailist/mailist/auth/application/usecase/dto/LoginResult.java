package com.mailist.mailist.auth.application.usecase.dto;

import com.mailist.mailist.auth.domain.aggregate.User;
import lombok.Builder;

@Builder
public record LoginResult(
        String accessToken,
        String refreshToken,
        User user,
        java.time.LocalDateTime expiresAt
) {
}
