package com.mailist.marketing.campaign.application.usecase;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendCampaignCommand {
    private Long campaignId;
    private String senderEmail;
}