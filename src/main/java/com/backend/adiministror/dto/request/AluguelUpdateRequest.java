package com.backend.adiministror.dto.request;

import com.backend.adiministror.model.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AluguelUpdateRequest(
        @NotNull(message = "A data de início deve ser informada!")
        LocalDate dataInicio,

        @NotNull(message = "A data de vencimento deve ser informada!")
        LocalDate dataVencimento,

        PaymentStatus status
) {
}
