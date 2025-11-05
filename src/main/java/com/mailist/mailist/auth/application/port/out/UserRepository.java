package com.mailist.mailist.auth.application.port.out;

import com.mailist.mailist.auth.domain.aggregate.User;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndTenantId(String email, Long tenantId);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    boolean existsByEmail(String email);

    void delete(User user);

    void deleteById(Long id);
}