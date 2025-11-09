package com.mailist.mailist.auth.infrastructure.repository;

import com.mailist.mailist.auth.domain.aggregate.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.tenantId = :tenantId")
    Optional<User> findByEmailAndTenantId(@Param("email") String email, @Param("tenantId") Long tenantId);

    Optional<User> findByVerificationToken(String verificationToken);

    Optional<User> findByPasswordResetToken(String passwordResetToken);

    boolean existsByEmail(String email);

    // Team management methods
    List<User> findAllByTenantId(Long tenantId);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.id = :userId")
    Optional<User> findByIdAndTenantId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.status = 'ACTIVE'")
    Integer countActiveByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.status = 'PENDING_VERIFICATION'")
    Integer countPendingByTenantId(@Param("tenantId") Long tenantId);
}