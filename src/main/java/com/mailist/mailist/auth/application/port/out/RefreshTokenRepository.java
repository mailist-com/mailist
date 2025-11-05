package com.mailist.mailist.auth.application.port.out;

import com.mailist.mailist.auth.domain.aggregate.RefreshToken;
import com.mailist.mailist.auth.domain.aggregate.User;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository {
    
    RefreshToken save(RefreshToken refreshToken);
    
    Optional<RefreshToken> findByToken(String token);
    
    List<RefreshToken> findByUser(User user);
    
    void delete(RefreshToken refreshToken);
    
    void deleteByUser(User user);
    
    void deleteExpiredTokens();
}