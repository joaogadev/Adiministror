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

    @OneToOne
    @JoinColumn(name = "inquilino_id")
    private UsuarioModel inquilino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "galeria_id", nullable = false)
    private GaleriaModel galeria;

    public SalasModel(
            String nome,
            UsuarioModel inquilino,
            GaleriaModel galeria
    ) {
        this.nome = nome;
        this.inquilino = inquilino;
        this.galeria = galeria;
    }
}
