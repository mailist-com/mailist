package com.mailist.mailist.campaign.domain.valueobject;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailTemplate {
    
    @Column(name = "html_content", columnDefinition = "TEXT")
    private String htmlContent;
    
    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;
    
    @Column(name = "template_name")
    private String templateName;
    
    @Column(name = "template_variables", columnDefinition = "TEXT")
    private String templateVariables;
    
    public boolean isValid() {
        return (htmlContent != null && !htmlContent.trim().isEmpty()) ||
               (textContent != null && !textContent.trim().isEmpty());
    }
}