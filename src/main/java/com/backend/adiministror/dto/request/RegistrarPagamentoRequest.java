package com.backend.adiministror.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RegistrarPagamentoRequest(
        @NotNull
        LocalDate dataPagamento
) {
}
