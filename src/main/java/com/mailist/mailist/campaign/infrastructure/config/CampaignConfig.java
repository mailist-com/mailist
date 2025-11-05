package com.mailist.mailist.campaign.infrastructure.config;

import com.mailist.mailist.campaign.application.port.out.CampaignRepository;
import com.mailist.mailist.campaign.application.usecase.CreateCampaignUseCase;
import com.mailist.mailist.campaign.application.usecase.ListCampaignsUseCase;
import com.mailist.mailist.campaign.application.usecase.ScheduleCampaignUseCase;
import com.mailist.mailist.campaign.application.usecase.SendCampaignUseCase;
import com.mailist.mailist.campaign.application.usecase.*;
import com.mailist.mailist.campaign.domain.gateway.EmailGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CampaignConfig {
    
    @Bean
    public CreateCampaignUseCase createCampaignUseCase(CampaignRepository campaignRepository) {
        return new CreateCampaignUseCase(campaignRepository);
    }
    
    @Bean
    public SendCampaignUseCase sendCampaignUseCase(
            CampaignRepository campaignRepository,
            EmailGateway emailGateway) {
        return new SendCampaignUseCase(campaignRepository, emailGateway);
    }
    
    @Bean
    public ScheduleCampaignUseCase scheduleCampaignUseCase(CampaignRepository campaignRepository) {
        return new ScheduleCampaignUseCase(campaignRepository);
    }
    
    @Bean
    public ListCampaignsUseCase listCampaignsUseCase(CampaignRepository campaignRepository) {
        return new ListCampaignsUseCase(campaignRepository);
    }
}