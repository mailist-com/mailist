package com.mailist.mailist.contact.application.usecase;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UpdateContactCommand {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Set<Long> listIds;
}
