package com.mailist.marketing.campaign.application.usecase;

import com.mailist.marketing.campaign.domain.aggregate.Campaign;
import com.mailist.marketing.campaign.domain.valueobject.EmailTemplate;
import com.mailist.marketing.campaign.application.port.out.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateCampaignUseCase {
    
    private final CampaignRepository campaignRepository;
    
    public Campaign execute(CreateCampaignCommand command) {
        EmailTemplate template = EmailTemplate.builder()
                .htmlContent(command.getHtmlContent())
                .textContent(command.getTextContent())
                .templateName(command.getTemplateName())
                .build();
        
        if (!template.isValid()) {
            throw new IllegalArgumentException("Campaign must have either HTML or text content");
        }
        
        Campaign campaign = Campaign.builder()
                .name(command.getName())
                .subject(command.getSubject())
                .template(template)
                .status(Campaign.CampaignStatus.DRAFT)
                .build();
        
        if (command.getRecipients() != null) {
            command.getRecipients().forEach(campaign::addRecipient);
        }
        
        return campaignRepository.save(campaign);
    }
}