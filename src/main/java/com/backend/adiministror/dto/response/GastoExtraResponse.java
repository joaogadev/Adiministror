package com.backend.adiministror.dto.response;

import com.backend.adiministror.model.GastoExtraModel;

import java.math.BigDecimal;
import java.util.UUID;

public record GastoExtraResponse(
        UUID id,
        String nome,
        String descricao,
        BigDecimal valor,
        String dataGasto,
        String nomeGaleria
) {
    public static GastoExtraResponse from(GastoExtraModel gastoExtra) {
        return new GastoExtraResponse(
                gastoExtra.getId(),
                gastoExtra.getNome(),
                gastoExtra.getDescricao(),
                gastoExtra.getValor(),
                gastoExtra.getDataGasto().toString(),
                gastoExtra.getGaleria().getNome()
        );
    }
}
