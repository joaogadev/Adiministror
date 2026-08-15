package com.backend.adiministror.dto;

import com.backend.adiministror.model.EnderecoModel;

public record EnderecoResponse(
        String zipCode,
        String estado,
        String cidade,
        String bairro,
        String rua,
        String numero,
        String complemento
) {
    public static EnderecoResponse from(EnderecoModel endereco) {
        return new EnderecoResponse(
                endereco.getZipCode(),
                endereco.getEstado(),
                endereco.getCidade(),
                endereco.getBairro(),
                endereco.getRua(),
                endereco.getNumero(),
                endereco.getComplemento()
        );
    }
}
