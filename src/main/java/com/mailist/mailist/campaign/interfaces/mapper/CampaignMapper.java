package com.mailist.mailist.campaign.interfaces.mapper;

import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import com.mailist.mailist.campaign.interfaces.dto.CampaignDto;
import com.mailist.mailist.campaign.application.usecase.CreateCampaignCommand;
import com.mailist.mailist.campaign.application.usecase.SendCampaignCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CampaignMapper {
    
    CreateCampaignCommand toCreateCommand(CampaignDto.CreateRequest request);
    
    @Mapping(target = "campaignId", source = "campaignId")
    SendCampaignCommand toSendCommand(Long campaignId, CampaignDto.SendRequest request);
    
    @Mapping(target = "status", expression = "java(campaign.getStatus().name())")
    @Mapping(target = "recipientCount", expression = "java(campaign.getRecipients().size())")
    CampaignDto.Response toResponse(Campaign campaign);
}