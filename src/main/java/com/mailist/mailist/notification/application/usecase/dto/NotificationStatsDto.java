package com.mailist.mailist.notification.application.usecase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatsDto {

    private Long total;
    private Long unread;
    private Map<String, Long> byCategory;
}
