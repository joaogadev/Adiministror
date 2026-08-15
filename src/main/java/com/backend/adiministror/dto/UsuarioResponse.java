package com.backend.adiministror.dto;

import com.backend.adiministror.model.Role;
import com.backend.adiministror.model.UsuarioModel;

import java.util.UUID;

public record UsuarioResponse (
        UUID id,
        String nome,
        String email,
        String phone,
        Role role
) {
    public static UsuarioResponse from(UsuarioModel user) {
        return new UsuarioResponse(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        );
    }
}
