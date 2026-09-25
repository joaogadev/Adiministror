package com.backend.adiministror.dto.request;

import com.backend.adiministror.model.enums.StatusAluguel;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AluguelUpdateRequest(
        @NotNull(message = "A data de início deve ser informada!")
        LocalDate dataInicio,

        @NotNull(message = "A data de vencimento deve ser informada!")
        Integer diaVencimentoPadrao,

        BigDecimal valorAluguel
) {
}
