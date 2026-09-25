package com.backend.adiministror.dto.response;

import java.time.LocalDate;
import java.util.Map;

public record ApiErrorResponse(
        LocalDate timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {
}
