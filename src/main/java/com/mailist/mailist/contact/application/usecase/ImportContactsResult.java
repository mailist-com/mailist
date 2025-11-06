package com.mailist.mailist.contact.application.usecase;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ImportContactsResult {
    private Integer imported;
    private Integer skipped;
    private Integer errors;

    @Builder.Default
    private List<ErrorDetail> errorDetails = new ArrayList<>();

    @Data
    @Builder
    public static class ErrorDetail {
        private Integer row;
        private String email;
        private String error;
    }
}
