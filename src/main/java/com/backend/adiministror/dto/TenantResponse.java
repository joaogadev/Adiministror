package com.backend.adiministror.dto;

import com.backend.adiministror.model.DocumentType;
import com.backend.adiministror.model.TenantModel;

import java.util.UUID;

public record TenantResponse (
        UUID id,
        String nome,
        String phone,
        String email,
        DocumentType documentType,
        String documentNumber
) {
    public static TenantResponse from(TenantModel tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getNome(),
                tenant.getPhone(),
                tenant.getEmail(),
                tenant.getDocumentType(),
                tenant.getDocumentNumber()
        );
    }
}
