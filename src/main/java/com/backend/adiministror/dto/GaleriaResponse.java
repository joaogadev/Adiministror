package com.backend.adiministror.dto;

import com.backend.adiministror.model.GaleriaModel;

import java.util.UUID;

public record GaleriaResponse(
    UUID id,
    String nome,
    String phone,
    EnderecoResponse endereco,
    UsuarioResponse dono
) {
    public static GaleriaResponse from(GaleriaModel galeria) {
        return new GaleriaResponse(
              galeria.getId(),
              galeria.getNome(),
              galeria.getPhone(),
              EnderecoResponse.from(galeria.getEndereco()),
              UsuarioResponse.from(galeria.getDono())
        );
    }
}
