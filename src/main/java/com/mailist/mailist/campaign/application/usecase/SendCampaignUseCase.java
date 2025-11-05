package com.mailist.mailist.campaign.application.usecase;

import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import com.mailist.mailist.shared.domain.model.EmailMessage;
import com.mailist.mailist.campaign.domain.gateway.EmailGateway;
import com.mailist.mailist.campaign.application.port.out.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class SendCampaignUseCase {
    
    private final CampaignRepository campaignRepository;
    private final EmailGateway emailGateway;
    
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
            EmailMessage emailMessage = EmailMessage.builder()
                    .from(command.getSenderEmail())
                    .to(recipient)
                    .subject(campaign.getSubject())
                    .htmlContent(campaign.getTemplate().getHtmlContent())
                    .textContent(campaign.getTemplate().getTextContent())
                    .campaignId(campaign.getId().toString())
                    .scheduledAt(LocalDateTime.now())
                    .build();
            
            try {
                emailGateway.sendEmail(emailMessage);
            } catch (Exception e) {
                // Log error and continue with other recipients
                System.err.println("Failed to send email to " + recipient + ": " + e.getMessage());
            }
        }
        
        campaign.send();
        return campaignRepository.save(campaign);
    }
}