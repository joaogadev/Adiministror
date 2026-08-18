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
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "phone")
    private String phone;

    public UsuarioModel(String nome, String email, String senha, String phone) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.phone = phone;
        this.role = Role.DONO;
    }

    public void atualizrDados(
            String nome, String email, String phone
    ) {
        this.nome = nome;
        this.email = email;
        this.phone = phone;
    }
    public void alterarSenha(String senha) {
        if (senha == null || senha.trim().isEmpty()) {
            throw new RuntimeException("Senha não pode ser vazia");
        }
        if (senha.length() < 8) {
            throw new RuntimeException("Senha deve ter no minimo 8 caracteres");
        }

        if (!senhaValida(senha)) throw new RuntimeException("Senha deve conter no mínimo 1 número e 1 caractere especial");

        this.senha = senha;
    }
    public static boolean senhaValida(String senha) {
        if (senha == null || senha.isEmpty()) {
            return false; // Senha vazia ou nula é inválida
        }
        boolean temNumero = senha.matches(".*\\d.*");
        boolean temEspecial = senha.matches(".*[^\\p{L}\\p{N}\\s].*");
        return temNumero && temEspecial;
    }
}
