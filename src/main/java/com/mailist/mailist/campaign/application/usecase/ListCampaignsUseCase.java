package com.mailist.mailist.campaign.application.usecase;

import com.mailist.mailist.campaign.application.port.out.CampaignRepository;
import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListCampaignsUseCase {
    
    private final CampaignRepository campaignRepository;
    
    public Page<Campaign> execute(ListCampaignsQuery query) {
        log.debug("Listing campaigns with pagination: {}", query.getPageable());
        
        return campaignRepository.findAll(query.getPageable());
    }
}