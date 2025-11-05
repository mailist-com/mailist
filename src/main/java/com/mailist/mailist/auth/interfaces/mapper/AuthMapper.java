package com.mailist.mailist.auth.interfaces.mapper;

import com.mailist.mailist.auth.application.usecase.*;
import com.mailist.mailist.auth.interfaces.dto.*;
import com.mailist.mailist.auth.application.usecase.*;
import com.mailist.mailist.auth.domain.aggregate.User;
import com.mailist.mailist.auth.interfaces.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    
    // Command mappings
    RegisterUserCommand toCommand(RegisterRequestDto dto);
    LoginCommand toCommand(LoginRequestDto dto);
    VerifyEmailCommand toCommand(VerifyEmailRequestDto dto);
    RequestPasswordResetCommand toCommand(RequestPasswordResetDto dto);
    ResetPasswordCommand toCommand(ResetPasswordRequestDto dto);
    RefreshTokenCommand toCommand(RefreshTokenRequestDto dto);
    Verify2FACommand toCommand(Verify2FARequestDto dto);
    
    // Response mappings
    default LoginResponseDto toLoginResponse(LoginUseCase.LoginResult loginResult) {
        User user = loginResult.getUser();

        LoginResponseDto.UserDto userDto = LoginResponseDto.UserDto.builder()
                .id(user.getId() != null ? user.getId().toString() : null)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(getPrimaryRole(user.getRoles()))
                .avatar(user.getAvatar())
                .build();

        return LoginResponseDto.success(
                userDto,
                loginResult.getAccessToken(),
                loginResult.getRefreshToken(),
                "Login successful"
        );
    }

    @Named("getPrimaryRole")
    default String getPrimaryRole(Set<User.Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return "user";
        }

        // Priority order: OWNER > ADMIN > USER
        if (roles.contains(User.Role.OWNER)) {
            return "owner";
        }
        if (roles.contains(User.Role.ADMIN)) {
            return "admin";
        }
        return "user";
    }
}