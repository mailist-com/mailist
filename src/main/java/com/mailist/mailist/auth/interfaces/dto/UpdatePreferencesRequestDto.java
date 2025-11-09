package com.mailist.mailist.auth.interfaces.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePreferencesRequestDto {

    @Size(max = 100, message = "Default from name must not exceed 100 characters")
    private String defaultFromName;

    @Email(message = "Default from email must be a valid email address")
    @Size(max = 100, message = "Default from email must not exceed 100 characters")
    private String defaultFromEmail;

    @Size(max = 1000, message = "Email signature must not exceed 1000 characters")
    private String emailSignature;

    private String dateFormat;
    private String timeFormat;
}
