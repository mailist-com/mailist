package com.mailist.mailist.auth.application.usecase.command;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UpdateProfileCommand {
    private Long userId;
    private String firstName;
    private String lastName;
    private String phone;
    private String company;
    private String timezone;
    private String language;
}
