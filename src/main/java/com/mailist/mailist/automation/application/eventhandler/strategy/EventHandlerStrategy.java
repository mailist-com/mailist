package com.mailist.mailist.automation.application.eventhandler.strategy;

import com.mailist.mailist.shared.domain.event.DomainEvent;

public interface EventHandlerStrategy<T extends DomainEvent> {
    
    void handle(T event);
    
    Class<T> getSupportedEventType();
}