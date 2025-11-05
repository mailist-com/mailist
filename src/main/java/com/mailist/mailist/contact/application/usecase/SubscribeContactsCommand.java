package com.mailist.mailist.contact.application.usecase;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SubscribeContactsCommand {
    private Long listId;
    private List<Long> contactIds;
}
