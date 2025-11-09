package com.mailist.mailist.auth.application.usecase.command;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class SetPasswordCommand {
    private String token;
    private String password;
}
