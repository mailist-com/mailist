package com.mailist.mailist.externalapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard response wrapper for external API endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private ExternalApiError error;

    public static <T> ExternalApiResponse<T> success(T data) {
        return ExternalApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ExternalApiResponse<T> success(T data, String message) {
        return ExternalApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ExternalApiResponse<T> error(String message, String code) {
        return ExternalApiResponse.<T>builder()
                .success(false)
                .error(ExternalApiError.builder()
                        .message(message)
                        .code(code)
                        .build())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExternalApiError {
        private String code;
        private String message;
    }
}
