package com.backend.adiministror.model;

import com.backend.adiministror.model.enums.DocumentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "tenant")
@Getter
@NoArgsConstructor
public class TenantModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", length = 255)
    private String nome;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "document_number")
    private String documentNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentType documentType;

    @Column(name = "ativo")
    private boolean ativo = true;

    public TenantModel(
            String nome,
            String phone,
            String email,
            String documentNumber,
            DocumentType documentType
    ) {
        this.nome = nome;
        this.phone = phone;
        this.email = email;
        this.documentNumber = documentNumber;
        this.documentType = DocumentType.valueOf(documentType.name());
    }

    public void atualizrDados(
            String nome, String phone, String email
    ) {
        this.nome = nome;
        this.phone = phone;
        this.email = email;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }
}
