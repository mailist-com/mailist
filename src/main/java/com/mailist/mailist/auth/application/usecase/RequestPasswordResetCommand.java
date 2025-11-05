package com.mailist.mailist.auth.application.usecase;

import lombok.Data;
import lombok.Builder;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
public class RequestPasswordResetCommand {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
}