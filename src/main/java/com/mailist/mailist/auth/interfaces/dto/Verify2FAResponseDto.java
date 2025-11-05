package com.mailist.mailist.auth.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Verify2FAResponseDto {

    private boolean success;
    private VerificationData data;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VerificationData {
        private boolean verified;
    }

    public static Verify2FAResponseDto success(boolean verified) {
        return Verify2FAResponseDto.builder()
                .success(true)
                .data(VerificationData.builder().verified(verified).build())
                .build();
    }
}
