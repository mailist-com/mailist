package com.mailist.mailist.campaign.application.usecase;

import com.mailist.mailist.billing.application.service.SubscriptionLimitService;
import com.mailist.mailist.billing.application.service.UsageTrackingService;
import com.mailist.mailist.campaign.application.usecase.command.SendCampaignCommand;
import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import com.mailist.mailist.campaign.infrastructure.repository.CampaignRepository;
import com.mailist.mailist.shared.domain.gateway.MarketingEmailGateway;
import com.mailist.mailist.shared.domain.model.MarketingEmailMessage;
import com.mailist.mailist.shared.infrastructure.security.SecurityUtils;
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
    private final SubscriptionLimitService subscriptionLimitService;
    private final UsageTrackingService usageTrackingService;

    Campaign execute(final SendCampaignCommand command) {
        final Campaign campaign = campaignRepository.findById(command.campaignId())
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

        if (campaign.getRecipients().isEmpty()) {
            throw new IllegalArgumentException("Campaign has no recipients");
        }

        if (!campaign.getTemplate().isValid()) {
            throw new IllegalArgumentException("Campaign template is not valid");
        }

        // Get tenant ID and check email sending limits
        Long tenantId = SecurityUtils.getTenantId();
        int emailCount = campaign.getRecipients().size();

        if (!subscriptionLimitService.canSendEmails(tenantId, emailCount)) {
            throw new IllegalStateException("Osiągnięto limit wysłanych emaili w tym miesiącu. " +
                    "Zwiększ plan aby wysłać więcej emaili.");
        }
        
        int successfulSends = 0;
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
                successfulSends++;
                log.debug("Marketing email sent to {} for campaign {}", recipient, campaign.getId());
            } catch (Exception e) {
                // Log error and continue with other recipients
                log.error("Failed to send marketing email to {} for campaign {}: {}",
                        recipient, campaign.getId(), e.getMessage());
            }
        }

        // Increment email sent count for successfully sent emails
        if (successfulSends > 0) {
            usageTrackingService.incrementEmailSentCount(tenantId, successfulSends);
            log.info("Incremented email sent count by {} for tenant {}", successfulSends, tenantId);
        }

        // Check and notify if approaching limits
        subscriptionLimitService.checkAndNotifyLimits(tenantId);

        campaign.send();
        return campaignRepository.save(campaign);
    }
}