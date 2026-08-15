package com.backend.adiministror.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.bind.DefaultValue;

public record UsuarioRequest(

        @Size(
                min = 3, max = 255,
                message = "O campo deve ter no mínimo 3 caracteres"
        )
        @DefaultValue("Dono")
        String nome,

        @Email(message = "O email é inválido")
        @NotBlank(message = "O email é obrigatório")
        @Size(max = 255, message = "O email deve ter no máximo 255 caracteres")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String senha,

        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "Informe um telefone válido, contendo apenas números e opcionalmente o sinal de +"
        )
        String phone
) {}