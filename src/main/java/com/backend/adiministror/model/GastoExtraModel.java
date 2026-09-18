package com.backend.adiministror.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "gasto_extra")
@Getter
@NoArgsConstructor
public class GastoExtraModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_gasto", nullable = false)
    private LocalDate dataGasto;

    @ManyToOne
    @JoinColumn(name = "galeria_id", nullable = false)
    private GaleriaModel galeria;

    public GastoExtraModel (String nome, String descricao, BigDecimal valor, LocalDate dataGasto, GaleriaModel galeria) {
        this.nome = nome;
        this.descricao = descricao;
        this.valor = valor;
        this.dataGasto = dataGasto;
        this.galeria = galeria;
    }

    public void atualizarDados(String nome, String descricao, BigDecimal valor, LocalDate dataGasto) {
        this.nome = nome;
        this.descricao = descricao;
        this.valor = valor;
        this.dataGasto = dataGasto;
    }
}
