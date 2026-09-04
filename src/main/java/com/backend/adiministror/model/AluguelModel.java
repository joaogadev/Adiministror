package com.backend.adiministror.model;

import com.backend.adiministror.model.enums.StatusAluguel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
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

    @Column(name = "dia_vencimento_padrao", nullable = false)
    private Integer diaVencimentoPadrao;

    private BigDecimal valorAluguel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusAluguel status;

    public AluguelModel(
            SalasModel salas,
            TenantModel inquilino,
            LocalDate dataInicio,
            Integer diaVencimento,
            BigDecimal valorAluguel, StatusAluguel ativo
    ) {
        this.sala = salas;
        this.inquilino = inquilino;
        this.dataInicio = dataInicio;
        this.diaVencimentoPadrao = diaVencimento;
        this.valorAluguel = valorAluguel;
        this.status = StatusAluguel.ATIVO;
    }

    public void atualizarDados(
            Integer diaVencimento,
            LocalDate dataInicio,
            BigDecimal valorAluguel
    ) {
        this.diaVencimentoPadrao = diaVencimento;
        this.dataInicio = dataInicio;
        this.valorAluguel = valorAluguel;
    }

    public void encerrar() {
        this.status = StatusAluguel.ENCERRADO;
    }
}
