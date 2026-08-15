package com.backend.adiministror.dto;

import com.backend.adiministror.model.DocumentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record TenantResquest (
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String name,

        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "Informe um telefone válido, contendo apenas números e opcionalmente o sinal de +"
        )
        String phone,

        @Email(message = "O email é inválido")
        @NotBlank(message = "O email é obrigatório")
        @Size(max = 255, message = "O email deve ter no máximo 255 caracteres")
        String email,

        @NotNull(message = "O tipo do documento não pode ser vazio")
        @Valid
        DocumentType documentType,

        @Pattern(
                regexp = "^\\+?[0-9]$",
                message = "O documento deve apresentar somente números"
        )
        String documentNumber
) {}
