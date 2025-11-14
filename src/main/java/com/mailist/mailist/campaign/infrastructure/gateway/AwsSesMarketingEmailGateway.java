package com.mailist.mailist.campaign.infrastructure.gateway;

import com.mailist.mailist.campaign.infrastructure.config.AwsSesProperties;
import com.mailist.mailist.shared.domain.gateway.MarketingEmailGateway;
import com.mailist.mailist.shared.domain.model.MarketingEmailMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * AWS SES implementation for marketing emails (campaigns, automation).
 * Uses AWS SES v2 API for sending emails with tracking capabilities.
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "aws.ses", name = "enabled", havingValue = "true")
public class AwsSesMarketingEmailGateway implements MarketingEmailGateway {

    private final SesV2Client sesClient;
    private final AwsSesProperties properties;

    @Value("${email.marketing.from.address}")
    private String defaultFromAddress;

    @Value("${email.marketing.from.name}")
    private String defaultFromName;

    public AwsSesMarketingEmailGateway(AwsSesProperties properties) {
        this.properties = properties;

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                properties.getAccessKeyId(),
                properties.getSecretAccessKey()
        );

        this.sesClient = SesV2Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .overrideConfiguration(config -> config
                        .apiCallTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                )
                .build();

        log.info("AWS SES Marketing Email Gateway initialized for region: {}", properties.getRegion());
    }

    @Override
    public void sendEmail(MarketingEmailMessage emailMessage) {
        try {
            // Build email content
            EmailContent emailContent = buildEmailContent(emailMessage);

            // Build destination
            Destination destination = Destination.builder()
                    .toAddresses(emailMessage.getTo())
                    .build();

            // Use default from address if not specified
            String fromAddress = emailMessage.getFrom() != null
                    ? emailMessage.getFrom()
                    : String.format("%s <%s>", defaultFromName, defaultFromAddress);

            // Build message tags for tracking
            List<MessageTag> tags = buildMessageTags(emailMessage);

            // Build send email request
            SendEmailRequest.Builder requestBuilder = SendEmailRequest.builder()
                    .fromEmailAddress(fromAddress)
                    .destination(destination)
                    .content(emailContent);

            // Add configuration set if specified
            if (properties.getConfigurationSetName() != null) {
                requestBuilder.configurationSetName(properties.getConfigurationSetName());
            }

            // Add email tags for tracking
            if (!tags.isEmpty()) {
                requestBuilder.emailTags(tags);
            }

            SendEmailRequest request = requestBuilder.build();

            // Send email
            SendEmailResponse response = sesClient.sendEmail(request);

            log.info("Marketing email sent successfully via AWS SES - To: {}, Campaign: {}, Message ID: {}",
                    emailMessage.getTo(), emailMessage.getCampaignId(), response.messageId());

        } catch (SesV2Exception e) {
            log.error("Failed to send marketing email to {} via AWS SES: {} - {}",
                    emailMessage.getTo(), e.awsErrorDetails().errorCode(), e.awsErrorDetails().errorMessage());
            throw new RuntimeException("Failed to send marketing email via AWS SES", e);
        } catch (Exception e) {
            log.error("Unexpected error sending marketing email to {} via AWS SES: {}",
                    emailMessage.getTo(), e.getMessage());
            throw new RuntimeException("Failed to send marketing email via AWS SES", e);
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            // Check if we can get account details (validates credentials and connectivity)
            GetAccountRequest request = GetAccountRequest.builder().build();
            sesClient.getAccount(request);
            log.debug("AWS SES Marketing email gateway health check passed");
            return true;
        } catch (Exception e) {
            log.warn("AWS SES Marketing email gateway health check failed: {}", e.getMessage());
            return false;
        }
    }

    private EmailContent buildEmailContent(MarketingEmailMessage emailMessage) {
        Body.Builder bodyBuilder = Body.builder();

        // Add HTML content if available
        if (emailMessage.getHtmlContent() != null) {
            Content htmlContent = Content.builder()
                    .data(emailMessage.getHtmlContent())
                    .charset("UTF-8")
                    .build();
            bodyBuilder.html(htmlContent);
        }

        // Add text content if available
        if (emailMessage.getTextContent() != null) {
            Content textContent = Content.builder()
                    .data(emailMessage.getTextContent())
                    .charset("UTF-8")
                    .build();
            bodyBuilder.text(textContent);
        }

        // Build subject
        Content subject = Content.builder()
                .data(emailMessage.getSubject())
                .charset("UTF-8")
                .build();

        // Build message
        Message message = Message.builder()
                .subject(subject)
                .body(bodyBuilder.build())
                .build();

        return EmailContent.builder()
                .simple(message)
                .build();
    }

    private List<MessageTag> buildMessageTags(MarketingEmailMessage emailMessage) {
        List<MessageTag> tags = new ArrayList<>();

        // Add tracking ID tag
        if (emailMessage.getTrackingId() != null) {
            tags.add(MessageTag.builder()
                    .name("TrackingID")
                    .value(emailMessage.getTrackingId())
                    .build());
        }

        // Add campaign ID tag
        if (emailMessage.getCampaignId() != null) {
            tags.add(MessageTag.builder()
                    .name("CampaignID")
                    .value(emailMessage.getCampaignId())
                    .build());
        }

        // Add contact ID tag
        if (emailMessage.getContactId() != null) {
            tags.add(MessageTag.builder()
                    .name("ContactID")
                    .value(emailMessage.getContactId())
                    .build());
        }

        // Add email type tag
        tags.add(MessageTag.builder()
                .name("EmailType")
                .value("MARKETING")
                .build());

        return tags;
    }
}
