package com.backend.adiministror.dto.response;

import com.backend.adiministror.model.AluguelModel;
import com.backend.adiministror.model.enums.StatusAluguel;

import java.util.UUID;

public record AluguelResponse(
        UUID id,
        UUID salaId,
        String salaNome,
        UUID tenantId,
        String tenantNome,
        String dataInicio,
        String dataVencimento,
        StatusAluguel status
) {
    public static AluguelResponse from(AluguelModel aluguel) {
        return new AluguelResponse(
                aluguel.getId(),
                aluguel.getSala().getId(),
                aluguel.getSala().getNome(),
                aluguel.getInquilino().getId(),
                aluguel.getInquilino().getNome(),
                aluguel.getDataInicio().toString(),
                aluguel.getDiaVencimentoPadrao().toString(),
                aluguel.getStatus()
        );
    }
}
