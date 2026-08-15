package com.backend.adiministror.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(

        @NotBlank(message = "O CEP não pode estar vazio")
        @Pattern(
                regexp = "^[0-9]{8}$",
                message = "O cnpj deve conter exatamente 8 números"

        )
        String zipCode,

        @NotBlank
        @Size(
                min = 2,
                max = 2,
                message = "Apenas 2 caracteres são permitidos para o estado"
        )
        String estado,

        @NotBlank
        @Size(max = 255)
        String cidade,

        @NotBlank
        @Size(max = 255)
        String bairro,

        @NotBlank
        @Size(max = 255)
        String rua,

        @NotBlank
        @Size(max = 255)
        String numero,

        @Size(max = 255)
        String complemento
) {
}
