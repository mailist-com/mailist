package com.mailist.marketing.contact.infrastructure.repository;

import com.mailist.marketing.contact.application.port.out.ContactRepository;
import com.mailist.marketing.contact.domain.aggregate.Contact;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ContactRepositoryImpl implements ContactRepository {
    
    private final ContactJpaRepository jpaRepository;
    
    @Override
    public Contact save(Contact contact) {
        return jpaRepository.save(contact);
    }
    
    @Override
    public Optional<Contact> findById(Long id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public Optional<Contact> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }
    
    @Override
    public Page<Contact> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }
    
    @Override
    public List<Contact> findByTagsName(String tagName) {
        return jpaRepository.findByTagsName(tagName);
    }
    
    @Override
    public List<Contact> findByLeadScoreBetween(int minScore, int maxScore) {
        return jpaRepository.findByLeadScoreBetween(minScore, maxScore);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public long count() {
        return jpaRepository.count();
    }
    
    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
}