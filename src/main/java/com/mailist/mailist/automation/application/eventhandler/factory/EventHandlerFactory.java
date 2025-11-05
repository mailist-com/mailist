package com.mailist.mailist.automation.application.eventhandler.factory;

import com.mailist.mailist.automation.application.eventhandler.strategy.EventHandlerStrategy;
import com.mailist.mailist.shared.domain.event.DomainEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventHandlerFactory {
    
    private final List<EventHandlerStrategy<? extends DomainEvent>> eventHandlers;
    private final Map<Class<? extends DomainEvent>, EventHandlerStrategy<? extends DomainEvent>> handlerMap = new HashMap<>();
    
    @PostConstruct
    public void initializeHandlers() {
        for (EventHandlerStrategy<? extends DomainEvent> handler : eventHandlers) {
            Class<? extends DomainEvent> eventType = handler.getSupportedEventType();
            handlerMap.put(eventType, handler);
            log.info("Registered event handler {} for event type {}", 
                    handler.getClass().getSimpleName(), eventType.getSimpleName());
        }
        log.info("Initialized {} event handlers", handlerMap.size());
    }
    
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> Optional<EventHandlerStrategy<T>> getHandler(Class<T> eventType) {
        EventHandlerStrategy<? extends DomainEvent> handler = handlerMap.get(eventType);
        if (handler != null) {
            return Optional.of((EventHandlerStrategy<T>) handler);
        }
        
        log.warn("No handler found for event type: {}", eventType.getSimpleName());
        return Optional.empty();
    }
    
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> Optional<EventHandlerStrategy<T>> getHandler(T event) {
        return getHandler((Class<T>) event.getClass());
    }
    
    public boolean hasHandler(Class<? extends DomainEvent> eventType) {
        return handlerMap.containsKey(eventType);
    }
    
    public int getHandlerCount() {
        return handlerMap.size();
    }
}