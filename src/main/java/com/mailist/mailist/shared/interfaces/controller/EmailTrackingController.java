package com.mailist.mailist.shared.interfaces.controller;

import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.automation.domain.event.EmailOpenedEvent;
import com.mailist.mailist.automation.domain.event.EmailClickedEvent;
import com.mailist.mailist.contact.infrastructure.repository.ContactRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Email Tracking", description = "Email tracking endpoints for opens and clicks")
public class EmailTrackingController {
    
    private final ApplicationEventPublisher eventPublisher;
    private final ContactRepository contactRepository;
    
    @GetMapping("/open")
    @Operation(summary = "Track email open event")
    public ResponseEntity<byte[]> trackEmailOpen(
            @RequestParam String contactEmail,
            @RequestParam String campaignId,
            @RequestParam String messageId,
            HttpServletResponse response) throws IOException {
        
        log.info("Email opened by {} for campaign {}", contactEmail, campaignId);
        
        // Find contact by email
        Contact contact = contactRepository.findByEmail(contactEmail).orElse(null);
        
        if (contact != null) {
            // Publish email opened event for automation
            var event = new EmailOpenedEvent(
                    contact.getId(),
                    contactEmail,
                    campaignId,
                    messageId
            );
            eventPublisher.publishEvent(event);
            
            // Update contact activity
            contact.updateActivity();
            contactRepository.save(contact);
        }
        
        // Return 1x1 transparent pixel
        byte[] pixel = new byte[] {
            (byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38, (byte) 0x39, (byte) 0x61,
            (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x80, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0x21, (byte) 0xF9, (byte) 0x04, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x2C, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x02, (byte) 0x02, (byte) 0x04, (byte) 0x01, (byte) 0x00,
            (byte) 0x3B
        };
        
        response.setContentType("image/gif");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        return ResponseEntity.ok(pixel);
    }
    
    @GetMapping("/click")
    @Operation(summary = "Track email click event and redirect")
    public void trackEmailClick(
            @RequestParam String contactEmail,
            @RequestParam String campaignId,
            @RequestParam String messageId,
            @RequestParam String url,
            HttpServletResponse response) throws IOException {
        
        log.info("Email clicked by {} for campaign {}, redirecting to {}", 
                contactEmail, campaignId, url);
        
        // Find contact by email
        Contact contact = contactRepository.findByEmail(contactEmail).orElse(null);
        
        if (contact != null) {
            // Publish email clicked event for automation
            var event = new EmailClickedEvent(
                    contact.getId(),
                    contactEmail,
                    campaignId,
                    messageId,
                    url
            );
            eventPublisher.publishEvent(event);
            
            // Update contact activity and lead score
            contact.updateActivity();
            contact.incrementLeadScore(5); // Award points for clicking
            contactRepository.save(contact);
        }
        
        // Redirect to the actual URL
        response.sendRedirect(url);
    }
}