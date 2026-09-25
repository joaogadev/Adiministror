package com.backend.adiministror.model;

import com.backend.adiministror.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "pagamento")
@Getter
@NoArgsConstructor
public class PagamentoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "aluguel_id", nullable = false)
    private AluguelModel aluguel;

    @Column(name = "competencia", nullable = false)
    private LocalDate competencia;

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    public PagamentoModel(
            AluguelModel aluguel,
            LocalDate competencia,
            BigDecimal valor,
            LocalDate dataVencimento
    ) {
        this.aluguel = aluguel;
        this.competencia = competencia;
        this.valor = valor;
        this.dataVencimento = dataVencimento;
        this.status = PaymentStatus.PENDENTE;
    }

    public void registrarPagamento(LocalDate dataPagamento) {
        this.status = PaymentStatus.PAGO;
        this.dataPagamento = dataPagamento;
    }

    public void alterarVencimento(LocalDate novaDataVencimento) {
        this.dataVencimento = novaDataVencimento;
    }

    public PaymentStatus getStatusAtual() {
        if (this.status == PaymentStatus.PAGO) {
            return PaymentStatus.PAGO;
        }

        if (LocalDate.now().isAfter(this.dataVencimento)) {
            return PaymentStatus.ATRASADO;
        }

        return PaymentStatus.PENDENTE;
    }
}
