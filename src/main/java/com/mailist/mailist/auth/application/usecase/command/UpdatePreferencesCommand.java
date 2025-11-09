package com.mailist.mailist.auth.application.usecase.command;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UpdatePreferencesCommand {
    private Long userId;
    private String defaultFromName;
    private String defaultFromEmail;
    private String emailSignature;
    private String dateFormat;
    private String timeFormat;
}
