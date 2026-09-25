package com.backend.adiministror.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AluguelRequest(

        @Valid
        @NotNull(message = "Inquilino deve ser informado!")
        TenantResquest inquilino,

        @NotNull
        LocalDate dataInicio,

        @NotNull(message = "A data de vencimento deve ser informada!")
        @Min(1)
        @Max(31)
        Integer diaVencimentoPadrao,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal valorAluguel,

        @Valid
        @NotNull
        ContratoRequest contrato
) {
}
