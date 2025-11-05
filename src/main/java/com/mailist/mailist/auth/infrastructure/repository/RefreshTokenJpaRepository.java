package com.mailist.mailist.auth.infrastructure.repository;

import com.mailist.mailist.auth.application.port.out.RefreshTokenRepository;
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
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long>, RefreshTokenRepository {

    @Override
    Optional<RefreshToken> findByToken(String token);

    @Override
    List<RefreshToken> findByUser(User user);

    @Override
    void deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.revoked = true")
    void deleteExpiredAndRevokedTokensInternal(@Param("now") LocalDateTime now);

    @Override
    default void deleteExpiredTokens() {
        deleteExpiredAndRevokedTokensInternal(LocalDateTime.now());
    }
}