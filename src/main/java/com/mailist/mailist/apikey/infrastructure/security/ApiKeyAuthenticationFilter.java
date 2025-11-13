package com.mailist.mailist.apikey.infrastructure.security;

import com.mailist.mailist.apikey.domain.aggregate.ApiKey;
import com.mailist.mailist.apikey.infrastructure.repository.ApiKeyRepository;
import com.mailist.mailist.shared.infrastructure.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Filter for authenticating API requests using API keys.
 * This filter is specifically for external API endpoints (/api/v1/external/**).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ApiKeyRepository apiKeyRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Only process requests to external API endpoints
        String path = request.getRequestURI();
        if (!path.startsWith("/api/v1/external/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String apiKey = extractApiKey(request);

            if (apiKey != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticateApiKey(apiKey, request);
            }
        } catch (Exception e) {
            log.error("API Key authentication error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract API key from request headers.
     * Supports both X-API-Key header and Authorization: Bearer header.
     */
    private String extractApiKey(HttpServletRequest request) {
        // Try X-API-Key header first
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey.trim();
        }

        // Try Authorization header with Bearer token
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length()).trim();
        }

        return null;
    }

    /**
     * Authenticate the API key and set up security context.
     */
    private void authenticateApiKey(String plainApiKey, HttpServletRequest request) {
        try {
            // Hash the provided API key
            String hashedKey = hashApiKey(plainApiKey);

            // Find API key in database
            Optional<ApiKey> apiKeyOpt = apiKeyRepository.findByKeyHash(hashedKey);

            if (apiKeyOpt.isEmpty()) {
                log.debug("API key not found");
                return;
            }

            ApiKey apiKey = apiKeyOpt.get();

            // Check if API key is active
            if (!apiKey.isActive()) {
                log.warn("Attempt to use inactive API key: {}", apiKey.getDisplayKey());
                return;
            }

            // Set tenant context
            TenantContext.setOrganizationId(apiKey.getTenantId());

            // Record usage
            apiKey.recordUsage(getClientIpAddress(request));
            apiKeyRepository.save(apiKey);

            // Create authentication token with permissions
            List<SimpleGrantedAuthority> authorities = apiKey.getPermissions().stream()
                    .map(permission -> new SimpleGrantedAuthority("ROLE_API_" + permission.toUpperCase().replace(".", "_")))
                    .collect(Collectors.toList());

            // Add a generic API role
            authorities.add(new SimpleGrantedAuthority("ROLE_API_USER"));

            ApiKeyAuthenticationToken authentication = new ApiKeyAuthenticationToken(
                    apiKey.getId().toString(),
                    apiKey,
                    authorities
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("API key authenticated successfully: {}", apiKey.getDisplayKey());

        } catch (Exception e) {
            log.error("Error authenticating API key: {}", e.getMessage(), e);
        }
    }

    /**
     * Hash the API key using SHA-256.
     */
    private String hashApiKey(String apiKey) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(apiKey.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Get client IP address from request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
