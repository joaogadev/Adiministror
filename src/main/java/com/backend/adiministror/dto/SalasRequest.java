package com.backend.adiministror.dto;

import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.bind.DefaultValue;

public record SalasRequest(
        @Size(
                min = 3, max = 255,
                message = "O campo deve ter no mínimo 3 caracteres"
        )
        @DefaultValue("Dono")
        String nome
) {
}
