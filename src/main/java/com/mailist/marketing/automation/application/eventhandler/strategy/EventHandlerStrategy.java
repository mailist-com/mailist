package com.mailist.marketing.automation.application.eventhandler.strategy;

import com.mailist.marketing.shared.domain.event.DomainEvent;

public interface EventHandlerStrategy<T extends DomainEvent> {
    
    void handle(T event);
    
    Class<T> getSupportedEventType();
}