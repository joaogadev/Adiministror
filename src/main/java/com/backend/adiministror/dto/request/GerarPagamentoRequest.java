package com.backend.adiministror.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record GerarPagamentoRequest(
        @NotNull
        LocalDate competencia
) {
}
