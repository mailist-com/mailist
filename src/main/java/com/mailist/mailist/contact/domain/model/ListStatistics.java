package com.mailist.mailist.contact.domain.model;

/**
 * Projection interface for global list statistics
 */
public interface ListStatistics {
    Long getTotalLists();
    Long getActiveLists();
    Long getTotalSubscribers();
}
