package com.mailist.mailist.campaign.application.usecase;

import com.mailist.mailist.campaign.application.usecase.command.CreateCampaignCommand;
import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import com.mailist.mailist.campaign.domain.valueobject.EmailTemplate;
import com.mailist.mailist.campaign.infrastructure.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
final class CreateCampaignUseCase {
    
    private final CampaignRepository campaignRepository;
    
    Campaign execute(final CreateCampaignCommand command) {
        final EmailTemplate template = EmailTemplate.builder()
                .htmlContent(command.htmlContent())
                .textContent(command.textContent())
                .templateName(command.templateName())
                .build();
        
        if (!template.isValid()) {
            throw new IllegalArgumentException("Campaign must have either HTML or text content");
        }
        
        final Campaign campaign = Campaign.builder()
                .name(command.name())
                .subject(command.subject())
                .template(template)
                .status(Campaign.CampaignStatus.DRAFT)
                .build();
        
        if (command.recipients() != null) {
            command.recipients().forEach(campaign::addRecipient);
        }
        
        return campaignRepository.save(campaign);
    }
}