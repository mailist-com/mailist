package com.mailist.mailist.contact.infrastructure.repository;

import com.mailist.mailist.contact.domain.aggregate.ContactList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactListJpaRepository extends JpaRepository<ContactList, Long> {
    
    Optional<ContactList> findByName(String name);
    
    boolean existsByName(String name);
    
    List<ContactList> findByIsActive(boolean isActive);
    
    List<ContactList> findByIsDynamic(boolean isDynamic);
    
    long countByIsActive(boolean isActive);
    
    @Query("SELECT cl FROM ContactList cl WHERE cl.isDynamic = false")
    List<ContactList> findStaticLists();
    
    @Query("SELECT cl FROM ContactList cl WHERE cl.isDynamic = true")
    List<ContactList> findDynamicLists();
    
    @Query("SELECT COUNT(c) FROM ContactList cl JOIN cl.contacts c WHERE cl.id = :listId")
    long countContactsInList(@Param("listId") Long listId);
}