package com.mailist.mailist.campaign.application.usecase;

import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import com.mailist.mailist.shared.domain.gateway.MarketingEmailGateway;
import com.mailist.mailist.shared.domain.model.MarketingEmailMessage;
import com.mailist.mailist.campaign.application.port.out.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SendCampaignUseCase {

    private final CampaignRepository campaignRepository;
    private final MarketingEmailGateway marketingEmailGateway;
    
    public Campaign execute(SendCampaignCommand command) {
        Campaign campaign = campaignRepository.findById(command.getCampaignId())
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        
        if (campaign.getRecipients().isEmpty()) {
            throw new IllegalArgumentException("Campaign has no recipients");
        }
        
        if (!campaign.getTemplate().isValid()) {
            throw new IllegalArgumentException("Campaign template is not valid");
        }
        
        for (String recipient : campaign.getRecipients()) {
            MarketingEmailMessage emailMessage = MarketingEmailMessage.builder()
                    .from(command.getSenderEmail())
                    .to(recipient)
                    .subject(campaign.getSubject())
                    .htmlContent(campaign.getTemplate().getHtmlContent())
                    .textContent(campaign.getTemplate().getTextContent())
                    .campaignId(campaign.getId().toString())
                    .trackingId(UUID.randomUUID().toString())
                    .scheduledAt(LocalDateTime.now())
                    .build();

            try {
                marketingEmailGateway.sendEmail(emailMessage);
                log.debug("Marketing email sent to {} for campaign {}", recipient, campaign.getId());
            } catch (Exception e) {
                // Log error and continue with other recipients
                log.error("Failed to send marketing email to {} for campaign {}: {}",
                        recipient, campaign.getId(), e.getMessage());
            }
        }
        
        campaign.send();
        return campaignRepository.save(campaign);
    }
}