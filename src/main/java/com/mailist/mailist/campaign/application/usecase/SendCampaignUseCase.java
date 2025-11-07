package com.mailist.mailist.campaign.application.usecase;

import com.mailist.mailist.campaign.application.usecase.command.SendCampaignCommand;
import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import com.mailist.mailist.campaign.infrastructure.repository.CampaignRepository;
import com.mailist.mailist.shared.domain.gateway.MarketingEmailGateway;
import com.mailist.mailist.shared.domain.model.MarketingEmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
class SendCampaignUseCase {

    private final CampaignRepository campaignRepository;
    private final MarketingEmailGateway marketingEmailGateway;
    
    Campaign execute(final SendCampaignCommand command) {
        final Campaign campaign = campaignRepository.findById(command.campaignId())
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
        
        if (campaign.getRecipients().isEmpty()) {
            throw new IllegalArgumentException("Campaign has no recipients");
        }
        
        if (!campaign.getTemplate().isValid()) {
            throw new IllegalArgumentException("Campaign template is not valid");
        }
        
        for (String recipient : campaign.getRecipients()) {
            final MarketingEmailMessage emailMessage = MarketingEmailMessage.builder()
                    .from(command.senderEmail())
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