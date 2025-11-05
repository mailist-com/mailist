package com.mailist.mailist.contact.application.port.out;

import com.mailist.mailist.contact.domain.aggregate.ContactList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ContactListRepository {
    
    ContactList save(ContactList contactList);
    
    Optional<ContactList> findById(Long id);
    
    Optional<ContactList> findByName(String name);
    
    List<ContactList> findByIsActive(boolean isActive);
    
    List<ContactList> findByIsDynamic(boolean isDynamic);
    
    Page<ContactList> findAll(Pageable pageable);
    
    void delete(ContactList contactList);
    
    void deleteById(Long id);
    
    boolean existsById(Long id);
    
    boolean existsByName(String name);
    
    long count();
    
    long countByIsActive(boolean isActive);
}