package com.mailist.mailist.campaign.infrastructure.gateway;

import com.mailist.mailist.shared.domain.gateway.EmailGateway;
import com.mailist.mailist.shared.domain.model.EmailMessage;
import com.mailist.mailist.campaign.infrastructure.config.EmailLabsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;
import java.util.HashMap;

@Component
@Slf4j
public class EmailLabsEmailGateway implements EmailGateway {
    
    private final WebClient webClient;
    private final EmailLabsProperties properties;

    @Autowired
    public EmailLabsEmailGateway(WebClient.Builder webClientBuilder, EmailLabsProperties properties) {
        this.properties = properties;
        this.webClient = webClientBuilder
                .baseUrl(properties.getApiBaseUrl())
                .defaultHeader("X-EmailLabs-ApiKey", properties.getApiKey())
                .defaultHeader("X-EmailLabs-Secret", properties.getSecret())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
    
    @Override
    public void sendEmail(EmailMessage emailMessage) {
        try {
            EmailLabsRequest request = buildEmailLabsRequest(emailMessage);
            
            EmailLabsResponse response = webClient.post()
                    .uri("/send")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(EmailLabsResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
            
            log.info("Email sent successfully to {} with message ID: {}", 
                    emailMessage.getTo(), response.getMessageId());
            
        } catch (WebClientResponseException e) {
            log.error("Failed to send email to {}: HTTP {} - {}", 
                    emailMessage.getTo(), e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to send email via EmailLabs", e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}", 
                    emailMessage.getTo(), e.getMessage());
            throw new RuntimeException("Failed to send email via EmailLabs", e);
        }
    }
    
    @Override
    public boolean isHealthy() {
        try {
            webClient.get()
                    .uri("/health")
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(10))
                    .block();
            return true;
        } catch (Exception e) {
            log.warn("EmailLabs health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    private EmailLabsRequest buildEmailLabsRequest(EmailMessage emailMessage) {
        Map<String, String> headers = new HashMap<>();
        if (emailMessage.getHeaders() != null) {
            headers.putAll(emailMessage.getHeaders());
        }
        
        if (emailMessage.getTrackingId() != null) {
            headers.put("X-Tracking-ID", emailMessage.getTrackingId());
        }
        
        if (emailMessage.getCampaignId() != null) {
            headers.put("X-Campaign-ID", emailMessage.getCampaignId());
        }
        
        return EmailLabsRequest.builder()
                .from(emailMessage.getFrom())
                .to(emailMessage.getTo())
                .subject(emailMessage.getSubject())
                .htmlContent(emailMessage.getHtmlContent())
                .textContent(emailMessage.getTextContent())
                .headers(headers)
                .build();
    }
    
    public static class EmailLabsRequest {
        private String from;
        private String to;
        private String subject;
        private String htmlContent;
        private String textContent;
        private Map<String, String> headers;
        
        public static EmailLabsRequestBuilder builder() {
            return new EmailLabsRequestBuilder();
        }
        
        public static class EmailLabsRequestBuilder {
            private EmailLabsRequest request = new EmailLabsRequest();
            
            public EmailLabsRequestBuilder from(String from) {
                request.from = from;
                return this;
            }
            
            public EmailLabsRequestBuilder to(String to) {
                request.to = to;
                return this;
            }
            
            public EmailLabsRequestBuilder subject(String subject) {
                request.subject = subject;
                return this;
            }
            
            public EmailLabsRequestBuilder htmlContent(String htmlContent) {
                request.htmlContent = htmlContent;
                return this;
            }
            
            public EmailLabsRequestBuilder textContent(String textContent) {
                request.textContent = textContent;
                return this;
            }
            
            public EmailLabsRequestBuilder headers(Map<String, String> headers) {
                request.headers = headers;
                return this;
            }
            
            public EmailLabsRequest build() {
                return request;
            }
        }
        
        // Getters for JSON serialization
        public String getFrom() { return from; }
        public String getTo() { return to; }
        public String getSubject() { return subject; }
        public String getHtmlContent() { return htmlContent; }
        public String getTextContent() { return textContent; }
        public Map<String, String> getHeaders() { return headers; }
    }
    
    public static class EmailLabsResponse {
        private String messageId;
        private String status;
        
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}