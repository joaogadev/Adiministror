package com.backend.adiministror.dto.request;

import com.backend.adiministror.model.enums.StatusContrato;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ContratoRequest(

        @NotNull
        LocalDate dataFim,

        @Min(1)
        Integer avisoAntecedenciaDias
) {
}
