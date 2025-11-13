package com.mailist.mailist.automation.application.eventhandler;

import com.mailist.mailist.automation.application.eventhandler.factory.EventHandlerFactory;
import com.mailist.mailist.automation.application.eventhandler.strategy.EventHandlerStrategy;
import com.mailist.mailist.automation.domain.event.EmailClickedEvent;
import com.mailist.mailist.automation.domain.event.EmailOpenedEvent;
import com.mailist.mailist.contact.domain.event.ContactCreatedEvent;
import com.mailist.mailist.contact.domain.event.ContactListJoinedEvent;
import com.mailist.mailist.contact.domain.event.ContactTagAddedEvent;
import com.mailist.mailist.automation.domain.event.*;
import com.mailist.mailist.contact.domain.event.*;
import com.mailist.mailist.shared.domain.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutomationEventHandler {

    private final EventHandlerFactory eventHandlerFactory;

    @EventListener
    @Async
    public void handleContactCreated(ContactCreatedEvent event) {
        handleEvent(event);
    }

    @EventListener
    @Async
    public void handleContactTagAdded(ContactTagAddedEvent event) {
        handleEvent(event);
    }

    @EventListener
    @Async
    public void handleEmailOpened(EmailOpenedEvent event) {
        handleEvent(event);
    }

    @EventListener
    @Async
    public void handleEmailClicked(EmailClickedEvent event) {
        handleEvent(event);
    }

    @EventListener
    @Async
    public void handleContactListJoined(ContactListJoinedEvent event) {
        handleEvent(event);
    }
    
    @SuppressWarnings("unchecked")
    private <T extends DomainEvent> void handleEvent(T event) {
        log.debug("Received event: {} with ID: {}", 
                event.getClass().getSimpleName(), event.getEventId());
        
        try {
            Optional<EventHandlerStrategy<T>> handlerOpt = eventHandlerFactory.getHandler(event);
            
            if (handlerOpt.isPresent()) {
                EventHandlerStrategy<T> handler = handlerOpt.get();
                log.info("Processing {} using {}", 
                        event.getClass().getSimpleName(), 
                        handler.getClass().getSimpleName());
                
                handler.handle(event);
                
                log.info("Successfully processed {} with ID: {}", 
                        event.getClass().getSimpleName(), event.getEventId());
            } else {
                log.warn("No handler found for event type: {}", 
                        event.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Failed to process event {} with ID {}: {}", 
                    event.getClass().getSimpleName(), event.getEventId(), e.getMessage(), e);
        }
    }
}