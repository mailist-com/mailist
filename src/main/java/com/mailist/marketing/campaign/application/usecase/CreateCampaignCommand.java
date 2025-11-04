package com.mailist.marketing.campaign.application.usecase;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCampaignCommand {
    private String name;
    private String subject;
    private String htmlContent;
    private String textContent;
    private String templateName;
    private Set<String> recipients;
}