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
    
    public static class Response {
        private Long id;
        private String name;
        private String subject;
        private String status;
        private Integer recipientCount;
        private LocalDateTime scheduledAt;
        private LocalDateTime sentAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Integer getRecipientCount() { return recipientCount; }
        public void setRecipientCount(Integer recipientCount) { this.recipientCount = recipientCount; }
        public LocalDateTime getScheduledAt() { return scheduledAt; }
        public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
        public LocalDateTime getSentAt() { return sentAt; }
        public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
}