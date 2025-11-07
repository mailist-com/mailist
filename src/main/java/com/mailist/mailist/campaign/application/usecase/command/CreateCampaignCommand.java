package com.mailist.mailist.campaign.application.usecase.command;

import lombok.Builder;
import java.util.Set;

@Builder
public record CreateCampaignCommand(
        String name,
        String subject,
        String htmlContent,
        String textContent,
        String templateName,
        Set<String> recipients
) { }