package com.backend.adiministror.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "galeria")
public class GaleriaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", length = 100, nullable = false,  unique = true)
    private String nome;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "quantidade_salas")
    private int quantidadeSalas;

    @OneToOne
    @JoinColumn(name = "endereco_id", nullable = false)
    private EnderecoModel endereco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dono_id", nullable = false)
    private UsuarioModel user;

    public GaleriaModel(
            String nome,
            String phone,
            int quantidadeSalas,
            EnderecoModel endereco,
            UsuarioModel user
    ) {
        this.nome = nome;
        this.phone = phone;
        this.quantidadeSalas = quantidadeSalas;
        this.endereco = endereco;
        this.user = user;
    }
}
