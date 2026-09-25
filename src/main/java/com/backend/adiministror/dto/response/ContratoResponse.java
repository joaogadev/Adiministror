package com.backend.adiministror.dto.response;

import com.backend.adiministror.model.ContratoModel;
import com.backend.adiministror.model.enums.StatusContrato;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;

public record ContratoResponse(
        String salaNome,
        String inquilinoNome,
        String proprietarioNome,
        LocalDate dataInicio,
        LocalDate dataFim,
        Integer avisoAntecedenciaDias,
        LocalDate dataAviso,
        StatusContrato status
) {
    public static ContratoResponse from(ContratoModel contrato) {
        return new ContratoResponse(
                contrato.getAluguel().getSala().getNome(),
                contrato.getAluguel().getInquilino().getNome(),
                contrato.getAluguel().getSala().getGaleria().getDono().getNome(),
                contrato.getDataInicio(),
                contrato.getDataFim(),
                contrato.getAvisoAntecedenciaDias(),
                contrato.getDataAviso(),
                contrato.getStatus()
        );
    }
}
