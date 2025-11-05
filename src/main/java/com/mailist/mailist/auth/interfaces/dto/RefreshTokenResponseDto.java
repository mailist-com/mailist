package com.mailist.mailist.auth.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenResponseDto {

    private boolean success;
    private TokenData data;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TokenData {
        private String token;
    }

    public static RefreshTokenResponseDto success(String token) {
        return RefreshTokenResponseDto.builder()
                .success(true)
                .data(TokenData.builder().token(token).build())
                .build();
    }
}
