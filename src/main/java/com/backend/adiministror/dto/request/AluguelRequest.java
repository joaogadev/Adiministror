package com.backend.adiministror.dto.request;

import com.backend.adiministror.model.PaymentStatus;
import com.backend.adiministror.model.TenantModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record AluguelRequest(

        @Valid
        @NotNull(message = "Inquilino deve ser informado!")
        TenantResquest inquilino,

        LocalDate dataInicio,

        @NotNull(message = "A data de vencimento deve ser informada!")
        LocalDate dataVencimento,

        @NotNull(message = "O status deve ser informado!")
        PaymentStatus status
) {
}
