package com.backend.adiministror.model;

import com.backend.adiministror.model.enums.StatusContrato;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contrato")
@Getter
@NoArgsConstructor
public class ContratoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "aluguel_id", nullable = false)
    private AluguelModel aluguel;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "aviso_antecedencia_dias", nullable = false)
    private Integer avisoAntecedenciaDias;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusContrato status;

    public ContratoModel(
            AluguelModel aluguel,
            LocalDate dataInicio,
            LocalDate dataFim,
            Integer avisoAntecedenciaDias
    ) {
        this.aluguel = aluguel;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.avisoAntecedenciaDias = avisoAntecedenciaDias;
        this.status = StatusContrato.ATIVO;
    }

    public void marcarComoRenovado() {
        this.status = StatusContrato.RENOVADO;
    }

    public void encerrar() {
        this.status = StatusContrato.ENCERRADO;
    }

    public LocalDate getDataAviso() {
        return dataFim.minusDays(avisoAntecedenciaDias);
    }
}
