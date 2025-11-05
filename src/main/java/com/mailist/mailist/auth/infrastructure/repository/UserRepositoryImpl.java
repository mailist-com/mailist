package com.mailist.mailist.auth.infrastructure.repository;

import com.mailist.mailist.auth.application.port.out.UserRepository;
import com.mailist.mailist.auth.domain.aggregate.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    
    private final UserJpaRepository userJpaRepository;
    
    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email);
    }
    
    @Override
    public Optional<User> findByEmailAndOrganizationId(String email, Long organizationId) {
        return userJpaRepository.findByEmailAndTenantId(email, organizationId);
    }
    
    @Override
    public Optional<User> findByVerificationToken(String token) {
        return userJpaRepository.findByVerificationToken(token);
    }
    
    @Override
    public Optional<User> findByPasswordResetToken(String token) {
        return userJpaRepository.findByPasswordResetToken(token);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }
    
    @Override
    public void delete(User user) {
        userJpaRepository.delete(user);
    }
    
    @Override
    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }
}