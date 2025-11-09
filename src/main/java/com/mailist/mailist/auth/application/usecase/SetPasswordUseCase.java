package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.usecase.command.SetPasswordCommand;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
final class SetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    void execute(final SetPasswordCommand command) {
        log.info("Setting password for user with token");

        // Find user by verification token
        final User user = userRepository.findByVerificationToken(command.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        // Validate token is still valid
        if (!user.isVerificationTokenValid()) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        // Set new password
        user.setPassword(passwordEncoder.encode(command.getPassword()));

        // Mark email as verified and activate account
        user.verifyEmail();

        // Save user
        userRepository.save(user);

        log.info("Password set successfully for user: {}", user.getEmail());
    }
}
