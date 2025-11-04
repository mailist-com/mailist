package com.mailist.marketing.auth.interfaces.mapper;

import com.mailist.marketing.auth.application.usecase.*;
import com.mailist.marketing.auth.domain.aggregate.User;
import com.mailist.marketing.auth.interfaces.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    
    // Command mappings
    RegisterUserCommand toCommand(RegisterRequestDto dto);
    LoginCommand toCommand(LoginRequestDto dto);
    VerifyEmailCommand toCommand(VerifyEmailRequestDto dto);
    RequestPasswordResetCommand toCommand(RequestPasswordResetDto dto);
    ResetPasswordCommand toCommand(ResetPasswordRequestDto dto);
    
    // Response mappings
    @Mapping(target = "user", source = "user")
    @Mapping(target = "tokenType", constant = "Bearer")
    LoginResponseDto toLoginResponse(LoginUseCase.LoginResult loginResult);
    
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    @Mapping(target = "organization", source = "organization")
    LoginResponseDto.UserDto toUserDto(User user);
    
    @Mapping(target = "plan", source = "plan")
    LoginResponseDto.UserDto.OrganizationDto toOrganizationDto(
        com.mailist.marketing.shared.domain.aggregate.Organization organization
    );
    
    @Named("rolesToStrings")
    default Set<String> rolesToStrings(Set<User.Role> roles) {
        return roles.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }
}