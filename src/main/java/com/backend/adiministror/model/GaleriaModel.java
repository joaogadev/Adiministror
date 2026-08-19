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

    @OneToOne
    @JoinColumn(name = "endereco_id", nullable = false)
    private EnderecoModel endereco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dono_id", nullable = false)
    private UsuarioModel dono;

    public GaleriaModel(
            String nome,
            String phone,
            EnderecoModel endereco,
            UsuarioModel dono
    ) {
        this.nome = nome;
        this.phone = phone;
        this.endereco = endereco;
        this.dono = dono;
    }

    public void atualizarDados(
            String nome,
            String phone
    ) {
        this.nome = nome;
        this.phone = phone;
    }
}
