package com.mailist.marketing.auth.infrastructure.repository;

import com.mailist.marketing.auth.application.port.out.RefreshTokenRepository;
import com.mailist.marketing.auth.domain.aggregate.RefreshToken;
import com.mailist.marketing.auth.domain.aggregate.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {
    
    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
    
    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return refreshTokenJpaRepository.save(refreshToken);
    }
    
    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenJpaRepository.findByToken(token);
    }
    
    @Override
    public List<RefreshToken> findByUser(User user) {
        return refreshTokenJpaRepository.findByUser(user);
    }
    
    @Override
    public void delete(RefreshToken refreshToken) {
        refreshTokenJpaRepository.delete(refreshToken);
    }
    
    @Override
    public void deleteByUser(User user) {
        refreshTokenJpaRepository.deleteByUser(user);
    }
    
    @Override
    public void deleteExpiredTokens() {
        refreshTokenJpaRepository.deleteExpiredAndRevokedTokens(LocalDateTime.now());
    }
}