package com.backend.adiministror.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AluguelRequest(

        @Valid
        @NotNull(message = "Inquilino deve ser informado!")
        TenantResquest inquilino,

        @NotNull(message = "A data de vencimento deve ser informada!")
        Integer diaVencimentoPadrao,

        BigDecimal valorAluguel
) {
}
