package com.mailist.mailist.auth.application.usecase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Verify2FACommand {
    private String code;
    private Long userId;
}
