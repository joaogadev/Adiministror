package com.backend.adiministror.dto.response;

import com.backend.adiministror.model.PagamentoModel;
import com.backend.adiministror.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PagamentoResponse(
        UUID id,

        UUID aluguelID,

        LocalDate competencia,

        BigDecimal valor,

        LocalDate dataVencimento,

        LocalDate dataPagamento,

        PaymentStatus status
) {
    public static PagamentoResponse from(PagamentoModel pagamento) {
        return new PagamentoResponse(
                pagamento.getId(),
                pagamento.getAluguel().getId(),
                pagamento.getCompetencia(),
                pagamento.getValor(),
                pagamento.getDataVencimento(),
                pagamento.getDataPagamento(),
                pagamento.getStatusAtual()
        );
    }
}
