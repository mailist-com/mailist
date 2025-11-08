package com.mailist.mailist.campaign.interfaces.mapper;

import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import com.mailist.mailist.campaign.interfaces.dto.CampaignDto;
import com.mailist.mailist.campaign.application.usecase.command.CreateCampaignCommand;
import com.mailist.mailist.campaign.application.usecase.command.SendCampaignCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CampaignMapper {
    
    CreateCampaignCommand toCreateCommand(CampaignDto.CreateRequest request);
    
    @Mapping(target = "campaignId", source = "campaignId")
    SendCampaignCommand toSendCommand(Long campaignId, CampaignDto.SendRequest request);
    
    @Mapping(target = "status", expression = "java(campaign.getStatus().name().toLowerCase())")
    @Mapping(target = "recipientCount", expression = "java(campaign.getRecipients().size())")
    @Mapping(target = "htmlContent", source = "template.htmlContent")
    @Mapping(target = "textContent", source = "template.textContent")
    @Mapping(target = "statistics", expression = "java(createEmptyStatistics())")
    CampaignDto.Response toResponse(Campaign campaign);

    default CampaignDto.Response.Statistics createEmptyStatistics() {
        return CampaignDto.Response.Statistics.builder()
            .sent(0)
            .delivered(0)
            .opens(0)
            .uniqueOpens(0)
            .clicks(0)
            .uniqueClicks(0)
            .bounces(0)
            .softBounces(0)
            .hardBounces(0)
            .unsubscribes(0)
            .complaints(0)
            .performance(CampaignDto.Response.Statistics.Performance.builder()
                .openRate(0.0)
                .clickRate(0.0)
                .clickToOpenRate(0.0)
                .bounceRate(0.0)
                .unsubscribeRate(0.0)
                .build())
            .build();
    }
}