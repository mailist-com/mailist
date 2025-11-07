package com.mailist.mailist.campaign.application.usecase.command;

import lombok.Builder;

@Builder
public record SendCampaignCommand(
        Long campaignId,
        String senderEmail
) { }