package com.mailist.mailist.externalapi.listener;

import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import com.mailist.mailist.apikey.infrastructure.repository.ApiKeyRepository;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.infrastructure.repository.ContactListRepository;
import com.mailist.mailist.externalapi.event.ExternalApiContactAddedEvent;
import com.mailist.mailist.externalapi.event.ExternalApiContactUpdatedEvent;
import com.mailist.mailist.notification.application.usecase.NotificationService;
import com.mailist.mailist.notification.application.usecase.dto.CreateNotificationRequest;
import com.mailist.mailist.notification.domain.aggregate.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for external API events.
 * Creates notifications when contacts are added or updated via external API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalApiEventListener {

    private final NotificationService notificationService;
    private final ApiKeyRepository apiKeyRepository;
    private final ContactListRepository contactListRepository;

    @EventListener
    @Async
    public void handleContactAdded(ExternalApiContactAddedEvent event) {
        try {
            // Get API key info
            ApiKey apiKey = apiKeyRepository.findById(event.getApiKeyId())
                    .orElse(null);

            if (apiKey == null) {
                log.warn("API key not found: {}", event.getApiKeyId());
                return;
            }

            // Build notification message
            StringBuilder message = new StringBuilder();
            message.append("Kontakt <b>").append(event.getContact().getEmail()).append("</b> ");
            message.append("został dodany przez zewnętrzny system ");
            message.append("(API Key: <b>").append(apiKey.getDisplayKey()).append("</b>)");

            // Add list info if available
            String listName = null;
            if (event.getListId() != null) {
                ContactList list = contactListRepository.findById(event.getListId())
                        .orElse(null);
                if (list != null) {
                    listName = list.getName();
                    message.append(" do listy <b>").append(listName).append("</b>");
                }
            }

            // Create notification
            notificationService.createNotification(
                    CreateNotificationRequest.builder()
                            .type(Notification.NotificationType.INFO)
                            .category(Notification.NotificationCategory.CONTACT_ADDED)
                            .title("Nowy kontakt z API")
                            .message(message.toString())
                            .contactEmail(event.getContact().getEmail())
                            .listName(listName)
                            .actionUrl("/contacts/view/" + event.getContact().getId())
                            .actionText("Zobacz kontakt")
                            .build()
            );

            log.info("Notification created for contact added via API: {}", event.getContact().getEmail());

        } catch (Exception e) {
            log.error("Error handling contact added event: {}", e.getMessage(), e);
        }
    }

    @EventListener
    @Async
    public void handleContactUpdated(ExternalApiContactUpdatedEvent event) {
        try {
            // Get API key info
            ApiKey apiKey = apiKeyRepository.findById(event.getApiKeyId())
                    .orElse(null);

            if (apiKey == null) {
                log.warn("API key not found: {}", event.getApiKeyId());
                return;
            }

            // Build notification message
            String message = "Kontakt <b>" + event.getContact().getEmail() + "</b> " +
                    "został zaktualizowany przez zewnętrzny system " +
                    "(API Key: <b>" + apiKey.getDisplayKey() + "</b>)";

            // Create notification
            notificationService.createNotification(
                    CreateNotificationRequest.builder()
                            .type(Notification.NotificationType.INFO)
                            .category(Notification.NotificationCategory.CONTACT_UPDATED)
                            .title("Kontakt zaktualizowany przez API")
                            .message(message)
                            .contactEmail(event.getContact().getEmail())
                            .actionUrl("/contacts/view/" + event.getContact().getId())
                            .actionText("Zobacz kontakt")
                            .build()
            );

            log.info("Notification created for contact updated via API: {}", event.getContact().getEmail());

        } catch (Exception e) {
            log.error("Error handling contact updated event: {}", e.getMessage(), e);
        }
    }
}
