package com.mailist.mailist.contact.application.usecase;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubscribeResult {
    private Integer subscribed;
    private Integer failed;
}
