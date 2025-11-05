package com.mailist.mailist.contact.infrastructure.repository;

import com.mailist.mailist.contact.application.port.out.ContactListRepository;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ContactListRepositoryImpl implements ContactListRepository {
    
    private final ContactListJpaRepository jpaRepository;
    
    @Override
    public ContactList save(ContactList contactList) {
        return jpaRepository.save(contactList);
    }
    
    @Override
    public Optional<ContactList> findById(Long id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public Optional<ContactList> findByName(String name) {
        return jpaRepository.findByName(name);
    }
    
    @Override
    public Page<ContactList> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }
    
    @Override
    public List<ContactList> findByIsActive(boolean isActive) {
        return jpaRepository.findByIsActive(isActive);
    }
    
    @Override
    public List<ContactList> findByIsDynamic(boolean isDynamic) {
        return jpaRepository.findByIsDynamic(isDynamic);
    }
    
    @Override
    public void delete(ContactList contactList) {
        jpaRepository.delete(contactList);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
    
    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
    
    @Override
    public long count() {
        return jpaRepository.count();
    }
    
    @Override
    public long countByIsActive(boolean isActive) {
        return jpaRepository.countByIsActive(isActive);
    }
}