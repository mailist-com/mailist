package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.port.out.EmailService;
import com.mailist.mailist.auth.application.usecase.command.InviteTeamMemberCommand;
import com.mailist.mailist.auth.application.usecase.dto.TeamMemberResult;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.infrastructure.repository.UserRepository;
import com.mailist.mailist.shared.domain.aggregate.Organization;
import com.mailist.mailist.shared.infrastructure.repository.OrganizationJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
final class InviteTeamMemberUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OrganizationJpaRepository organizationRepository;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    TeamMemberResult execute(final InviteTeamMemberCommand command) {
        log.info("Inviting team member: {} by user ID: {}", command.getEmail(), command.getInviterId());

        // Verify inviter exists and has permission
        final User inviter = userRepository.findById(command.getInviterId())
                .orElseThrow(() -> new IllegalArgumentException("Inviter not found"));

        if (!inviter.isOwner() && !inviter.isAdmin()) {
            throw new IllegalArgumentException("Only owners and admins can invite team members");
        }

        // Check if user already exists
        if (userRepository.findByEmail(command.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        // Parse role
        User.Role role;
        try {
            role = User.Role.valueOf(command.getRole().toUpperCase());
            if (role == User.Role.OWNER) {
                throw new IllegalArgumentException("Cannot invite a user as OWNER");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + command.getRole());
        }

        // Create new user with temporary password
        final String tempPassword = UUID.randomUUID().toString();
        final User newUser = User.builder()
                .email(command.getEmail())
                .firstName(command.getFirstName())
                .lastName(command.getLastName())
                .password(passwordEncoder.encode(tempPassword))
                .roles(Set.of(role))
                .status(User.Status.PENDING_VERIFICATION)
                .emailVerified(false)
                .build();

        // Set tenant ID and verification token
        newUser.setTenantId(inviter.getTenantId());
        newUser.setVerificationToken(UUID.randomUUID().toString());

        final User savedUser = userRepository.save(newUser);
        log.info("Successfully invited team member: {} with ID: {}", savedUser.getEmail(), savedUser.getId());

        // Get organization details for email
        final Organization organization = ((org.springframework.data.repository.CrudRepository<Organization, Long>) organizationRepository)
                .findById(inviter.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        // Build set password URL
        final String setPasswordUrl = String.format("%s/set-password?token=%s", frontendUrl, savedUser.getVerificationToken());

        // Send invitation email
        try {
            final String inviterFullName = inviter.getFirstName() + " " + inviter.getLastName();
            emailService.sendTeamInvitationEmail(
                    savedUser.getEmail(),
                    savedUser.getFirstName(),
                    inviterFullName,
                    organization.getName(),
                    setPasswordUrl
            );
            log.info("Invitation email sent to: {}", savedUser.getEmail());
        } catch (Exception e) {
            log.error("Failed to send invitation email to: {}", savedUser.getEmail(), e);
            // We still return success even if email fails - user is created
        }

        return TeamMemberResult.builder()
                .user(savedUser)
                .build();
    }
}
