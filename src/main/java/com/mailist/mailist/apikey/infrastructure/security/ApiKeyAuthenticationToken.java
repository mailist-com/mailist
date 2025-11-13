package com.mailist.mailist.apikey.infrastructure.security;

import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Custom authentication token for API key authentication.
 */
@Getter
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final String principal;
    private final ApiKey apiKey;

    public ApiKeyAuthenticationToken(
            String principal,
            ApiKey apiKey,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.principal = principal;
        this.apiKey = apiKey;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null; // No credentials needed after authentication
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    /**
     * Get the organization/tenant ID associated with this API key.
     */
    public Long getTenantId() {
        return apiKey.getTenantId();
    }

    /**
     * Get the API key ID.
     */
    public Long getApiKeyId() {
        return apiKey.getId();
    }

    /**
     * Check if the API key has a specific permission.
     */
    public boolean hasPermission(String permission) {
        return apiKey.hasPermission(permission);
    }
}
