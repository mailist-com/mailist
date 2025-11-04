package com.mailist.marketing.contact.application.usecase;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateContactCommand {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}