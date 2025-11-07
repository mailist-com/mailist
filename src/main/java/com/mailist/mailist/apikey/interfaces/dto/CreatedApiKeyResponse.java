package com.mailist.mailist.apikey.interfaces.dto;

/**
 * Response after creating a new API key.
 * Contains the plain API key - shown only once!
 */
public record CreatedApiKeyResponse(
        ApiKeyDto apiKey,
        String plainKey,
        String message
) {
}
