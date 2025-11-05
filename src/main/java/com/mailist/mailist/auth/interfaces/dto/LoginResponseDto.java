package com.mailist.mailist.auth.interfaces.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {

    private boolean success;
    private LoginData data;
    private String message;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginData {
        private UserDto user;
        private String token;
        private String refreshToken;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDto {
        private String id;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private String avatar;
    }

    public static LoginResponseDto success(UserDto user, String token, String refreshToken, String message) {
        return LoginResponseDto.builder()
                .success(true)
                .data(LoginData.builder()
                        .user(user)
                        .token(token)
                        .refreshToken(refreshToken)
                        .build())
                .message(message)
                .build();
    }
}