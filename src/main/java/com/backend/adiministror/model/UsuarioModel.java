package com.backend.adiministror.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "usuario")
public class UsuarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome")
    private String nome;

    @Column(name = "email", unique = true,  nullable = false)
    private String email;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "phone")
    private String phone;

    public UsuarioModel(String nome, String email, String senha, Role role, String phone) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.phone = phone;
    }
}
