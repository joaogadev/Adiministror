package com.backend.adiministror.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Table(name = "alugueis")
@Getter
public class AluguelModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id")
    private SalasModel sala;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquilino_id")
    private TenantModel inquilino;

    @CreationTimestamp
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @CreationTimestamp
    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pagamento", nullable = false)
    private PaymentStatus status;

    public AluguelModel(
            SalasModel salas,
            TenantModel inquilino,
            LocalDate dataInicio,
            LocalDate dataVencimento,
            PaymentStatus status
    ) {
        this.sala = salas;
        this.inquilino = inquilino;
        this.dataInicio = dataInicio;
        this.dataVencimento = dataVencimento;
        this.status = PaymentStatus.PENDENTE;
    }

    public void atualizarDados(
            LocalDate dataVencimento,
            LocalDate dataInicio,
            PaymentStatus status
    ) {
        this.dataVencimento = dataVencimento;
        this.dataInicio = dataInicio;
        this.status = status;
    }
}
