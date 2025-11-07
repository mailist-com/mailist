package com.mailist.mailist.auth.application.usecase.dto;

import lombok.Builder;

@Builder
public record RefreshTokenResult(String token) {
}
