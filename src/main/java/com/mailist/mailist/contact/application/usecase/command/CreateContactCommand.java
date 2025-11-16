package com.mailist.mailist.contact.application.usecase.command;

import com.mailist.mailist.contact.domain.valueobject.Tag;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateContactCommand {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Set<Long> listIds;
    private Set<Tag> tags;
}