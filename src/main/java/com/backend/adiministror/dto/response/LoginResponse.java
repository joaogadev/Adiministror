package com.backend.adiministror.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
