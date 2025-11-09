package com.mailist.mailist.auth.application.usecase;

import com.mailist.mailist.auth.application.usecase.command.*;
import com.mailist.mailist.auth.application.usecase.dto.LoginResult;
import com.mailist.mailist.auth.application.usecase.dto.RefreshTokenResult;
import com.mailist.mailist.auth.application.usecase.dto.Verify2FAResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthApplicationService {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final Verify2FAUseCase verify2FAUseCase;
    private final SetPasswordUseCase setPasswordUseCase;

    public void register(final RegisterUserCommand command) {
        registerUserUseCase.execute(command);
    }

    public void verifyEmail(final VerifyEmailCommand command) {
        verifyEmailUseCase.execute(command);
    }

    public LoginResult login(final LoginCommand command) {
        return loginUseCase.execute(command);
    }

    public void requestPasswordReset(final RequestPasswordResetCommand command) {
        requestPasswordResetUseCase.execute(command);
    }

    public void resetPassword(final ResetPasswordCommand command) {
        resetPasswordUseCase.execute(command);
    }

    public RefreshTokenResult refreshToken(final RefreshTokenCommand command) {
        return refreshTokenUseCase.execute(command);
    }

    public void logout(final long userId) {
        logoutUseCase.execute(userId);
    }

    public Verify2FAResult verify2FA(final Verify2FACommand command) {
        return verify2FAUseCase.execute(command);
    }

    public void setPassword(final SetPasswordCommand command) {
        setPasswordUseCase.execute(command);
    }
}
