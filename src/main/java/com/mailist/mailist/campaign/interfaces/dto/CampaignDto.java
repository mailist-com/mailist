package com.mailist.mailist.campaign.interfaces.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.Set;

public class CampaignDto {
    
    public static class CreateRequest {
        @NotBlank(message = "Campaign name is required")
        private String name;
        
        @NotBlank(message = "Subject is required")
        private String subject;
        
        private String htmlContent;
        private String textContent;
        private String templateName;
        
        @NotEmpty(message = "At least one recipient is required")
        private Set<String> recipients;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getHtmlContent() { return htmlContent; }
        public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }
        public String getTextContent() { return textContent; }
        public void setTextContent(String textContent) { this.textContent = textContent; }
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        public Set<String> getRecipients() { return recipients; }
        public void setRecipients(Set<String> recipients) { this.recipients = recipients; }
    }
    
    public static class SendRequest {
        @NotBlank(message = "Sender email is required")
        private String senderEmail;
        
        public String getSenderEmail() { return senderEmail; }
        public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String subject;
        private String preheader;
        private String fromName;
        private String fromEmail;
        private String replyTo;
        private String status;
        private String type;
        private String htmlContent;
        private String textContent;
        private Integer recipientCount;
        private LocalDateTime scheduledAt;
        private LocalDateTime sentAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Statistics statistics;

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Statistics {
            private Integer sent;
            private Integer delivered;
            private Integer opens;
            private Integer uniqueOpens;
            private Integer clicks;
            private Integer uniqueClicks;
            private Integer bounces;
            private Integer softBounces;
            private Integer hardBounces;
            private Integer unsubscribes;
            private Integer complaints;
            private Performance performance;

            @Data
            @Builder
            @AllArgsConstructor
            @NoArgsConstructor
            public static class Performance {
                private Double openRate;
                private Double clickRate;
                private Double clickToOpenRate;
                private Double bounceRate;
                private Double unsubscribeRate;
            }
        }
    }
}