package com.mailist.mailist.campaign.application.usecase;

import lombok.Value;
import java.time.LocalDateTime;

@Value
public class ScheduleCampaignCommand {
    Long campaignId;
    LocalDateTime scheduledAt;
}