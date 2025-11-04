package com.mailist.marketing.auth.application.usecase;

import com.mailist.marketing.auth.application.port.out.EmailService;
import com.mailist.marketing.auth.application.port.out.UserRepository;
import com.mailist.marketing.auth.domain.aggregate.User;
import com.mailist.marketing.shared.application.port.out.OrganizationRepository;
import com.mailist.marketing.shared.domain.aggregate.Organization;
import com.mailist.marketing.shared.infrastructure.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RegisterUserUseCase {
    
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    public User execute(RegisterUserCommand command) {
        log.info("Starting user registration for email: {}", command.getEmail());
        
        // Check if user already exists
        if (userRepository.existsByEmail(command.getEmail())) {
            throw new IllegalArgumentException("User with email " + command.getEmail() + " already exists");
        }
        
        // Check if subdomain is available
        if (organizationRepository.findBySubdomain(command.getSubdomain()).isPresent()) {
            throw new IllegalArgumentException("Subdomain " + command.getSubdomain() + " is already taken");
        }
        
        // Create organization (tenant)
        Organization organization = Organization.builder()
                .name(command.getOrganizationName())
                .subdomain(command.getSubdomain())
                .ownerEmail(command.getEmail())
                .plan(Organization.Plan.FREE)
                .status(Organization.Status.ACTIVE)
                .contactLimit(Organization.Plan.FREE.getContactLimit())
                .campaignLimit(Organization.Plan.FREE.getCampaignLimit())
                .automationLimit(Organization.Plan.FREE.getAutomationLimit())
                .build();
        
        organization = organizationRepository.save(organization);
        log.info("Created organization with ID: {} and subdomain: {}", organization.getId(), organization.getSubdomain());
        
        // Set tenant context for user creation
        TenantContext.setOrganizationId(organization.getId());
        
        try {
            // Generate verification token
            String verificationToken = generateVerificationCode();
            
            // Create user as organization owner
            User user = User.builder()
                    .email(command.getEmail())
                    .password(passwordEncoder.encode(command.getPassword()))
                    .firstName(command.getFirstName())
                    .lastName(command.getLastName())
                    .organization(organization)
                    .roles(Set.of(User.Role.OWNER, User.Role.ADMIN))
                    .status(User.Status.PENDING_VERIFICATION)
                    .emailVerified(false)
                    .build();
            
            user.setVerificationToken(verificationToken);
            user = userRepository.save(user);
            
            // Send verification email
            emailService.sendVerificationEmail(
                user.getEmail(), 
                verificationToken, 
                user.getFirstName()
            );
            
            log.info("Created user with ID: {} for organization: {} and sent verification email", 
                    user.getId(), organization.getId());
            
            return user;
        } finally {
            TenantContext.clear();
        }
    }
    
    private String generateVerificationCode() {
        // Generate 6-digit numeric code
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}