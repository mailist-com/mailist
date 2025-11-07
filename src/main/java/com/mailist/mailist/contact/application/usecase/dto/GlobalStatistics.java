package com.mailist.mailist.contact.application.usecase.dto;

import lombok.Builder;

@Builder
public record GlobalStatistics(
        Integer totalLists,
        Integer activeLists,
        Integer totalSubscribers,
        Double averageEngagement
) {
}
