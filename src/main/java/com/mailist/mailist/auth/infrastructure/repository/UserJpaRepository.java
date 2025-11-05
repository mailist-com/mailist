package com.mailist.mailist.auth.infrastructure.repository;

import com.mailist.mailist.auth.domain.aggregate.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.tenantId = :tenantId")
    Optional<User> findByEmailAndTenantId(@Param("email") String email, @Param("tenantId") Long tenantId);
    
    Optional<User> findByVerificationToken(String verificationToken);
    
    Optional<User> findByPasswordResetToken(String passwordResetToken);
    
    boolean existsByEmail(String email);
}