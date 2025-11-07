package com.mailist.mailist.auth.infrastructure.repository;

import com.mailist.mailist.auth.domain.aggregate.RefreshToken;
import com.mailist.mailist.auth.domain.aggregate.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser(User user);

    void deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.revoked = true")
    void deleteExpiredAndRevokedTokensInternal(@Param("now") LocalDateTime now);

    default void deleteExpiredTokens() {
        deleteExpiredAndRevokedTokensInternal(LocalDateTime.now());
    }
}