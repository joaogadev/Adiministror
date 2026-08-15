package com.backend.adiministror.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "salas")
public class SalasModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 100,  nullable = false,  unique = true)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "galeria_id", nullable = false)
    private GaleriaModel galeria;

    public SalasModel(
            String nome,
            GaleriaModel galeria
    ) {
        this.nome = nome;
        this.galeria = galeria;
    }
}
