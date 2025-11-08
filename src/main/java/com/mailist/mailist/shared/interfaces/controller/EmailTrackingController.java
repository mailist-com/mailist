package com.mailist.mailist.shared.interfaces.controller;

import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.automation.domain.event.EmailOpenedEvent;
import com.mailist.mailist.automation.domain.event.EmailClickedEvent;
import com.mailist.mailist.contact.infrastructure.repository.ContactRepository;
import com.mailist.mailist.analytics.domain.aggregate.EmailEvent;
import com.mailist.mailist.analytics.infrastructure.repository.EmailEventRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Email Tracking", description = "Email tracking endpoints for opens and clicks")
public class EmailTrackingController {

    private final ApplicationEventPublisher eventPublisher;
    private final ContactRepository contactRepository;
    private final EmailEventRepository emailEventRepository;
    
    @GetMapping("/open")
    @Operation(summary = "Track email open event")
    public ResponseEntity<byte[]> trackEmailOpen(
            @RequestParam String contactEmail,
            @RequestParam String campaignId,
            @RequestParam String messageId,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        log.info("Email opened by {} for campaign {}", contactEmail, campaignId);

        // Find contact by email
        Contact contact = contactRepository.findByEmail(contactEmail).orElse(null);

        if (contact != null) {
            // Save email event to database
            EmailEvent emailEvent = EmailEvent.builder()
                    .campaignId(campaignId)
                    .messageId(messageId)
                    .contactEmail(contactEmail)
                    .contactId(contact.getId())
                    .eventType(EmailEvent.EmailEventType.OPENED)
                    .userAgent(request.getHeader("User-Agent"))
                    .ipAddress(getClientIpAddress(request))
                    .createdAt(LocalDateTime.now())
                    .build();
            emailEventRepository.save(emailEvent);

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
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        log.info("Email clicked by {} for campaign {}, redirecting to {}",
                contactEmail, campaignId, url);

        // Find contact by email
        Contact contact = contactRepository.findByEmail(contactEmail).orElse(null);

        if (contact != null) {
            // Save email event to database
            EmailEvent emailEvent = EmailEvent.builder()
                    .campaignId(campaignId)
                    .messageId(messageId)
                    .contactEmail(contactEmail)
                    .contactId(contact.getId())
                    .eventType(EmailEvent.EmailEventType.CLICKED)
                    .clickedUrl(url)
                    .userAgent(request.getHeader("User-Agent"))
                    .ipAddress(getClientIpAddress(request))
                    .createdAt(LocalDateTime.now())
                    .build();
            emailEventRepository.save(emailEvent);

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

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}