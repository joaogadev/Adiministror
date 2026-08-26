package com.backend.adiministror.dto;

import com.backend.adiministror.model.SalasModel;

import java.util.UUID;

public record SalasResponse(
        UUID id,
        String nome,
        UUID tenantId,
        String tenantNome
) {
    public static SalasResponse from(SalasModel sala) {
        return new SalasResponse(
                sala.getId(),
                sala.getNome(),
                null,
                null
        );
    }
}
