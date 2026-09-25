package com.backend.adiministror.dto.request;

import com.backend.adiministror.model.enums.PaymentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagamentoRequest(
        @Valid
        @NotNull(message = "Aluguel deve ser informado!")
        AluguelRequest aluguel,

        LocalDate competencia,

        @NotNull(message = "O valor deve ser informado!")
        BigDecimal valor,

        LocalDate dataVencimento,

        LocalDate dataPagamento,

        @NotNull(message = "O status deve ser informado!")
        PaymentStatus status
) {
}
