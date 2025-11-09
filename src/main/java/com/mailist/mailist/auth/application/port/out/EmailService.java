package com.mailist.mailist.auth.application.port.out;

public interface EmailService {

    void sendVerificationEmail(String email, String verificationCode, String firstName);

    void sendPasswordResetEmail(String email, String resetCode, String firstName);

    void sendWelcomeEmail(String email, String firstName);

    void sendTeamInvitationEmail(String email, String firstName, String inviterName, String organizationName, String setPasswordUrl);
}