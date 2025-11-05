package com.mailist.mailist.campaign.application.usecase;

import com.mailist.mailist.campaign.application.port.out.CampaignRepository;
import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleCampaignUseCase {
    
    private final CampaignRepository campaignRepository;
    
    @Transactional
    public Campaign execute(ScheduleCampaignCommand command) {
        log.info("Scheduling campaign ID: {} for {}", command.getCampaignId(), command.getScheduledAt());
        
        Campaign campaign = campaignRepository.findById(command.getCampaignId())
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found with ID: " + command.getCampaignId()));
        
        campaign.schedule(command.getScheduledAt());
        
        Campaign savedCampaign = campaignRepository.save(campaign);
        log.info("Successfully scheduled campaign ID: {} for {}", savedCampaign.getId(), command.getScheduledAt());
        
        return savedCampaign;
    }
}