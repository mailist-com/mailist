package com.mailist.mailist.auth.application.usecase.dto;

import lombok.Builder;

@Builder
public record Verify2FAResult(boolean verified) {
}
