package com.mailist.mailist.auth.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Verify2FARequestDto {

    @NotBlank(message = "2FA code is required")
    private String code;

    @NotNull(message = "User ID is required")
    private Long userId;
}
