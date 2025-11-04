package com.mailist.marketing.auth.interfaces.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private LocalDateTime expiresAt;
    private UserDto user;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDto {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private Set<String> roles;
        private OrganizationDto organization;
        
        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class OrganizationDto {
            private Long id;
            private String name;
            private String subdomain;
            private String plan;
        }
    }
}