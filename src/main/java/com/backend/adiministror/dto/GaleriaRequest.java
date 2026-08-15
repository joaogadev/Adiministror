package com.backend.adiministror.dto;

import com.backend.adiministror.model.EnderecoModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GaleriaRequest(

        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "Informe um telefone válido, contendo apenas números e opcionalmente o sinal de +"
        )
        String phone,

        @NotNull(message = "O endereço não pode ser vazio")
        @Valid
        EnderecoRequest endereco
) {
}
