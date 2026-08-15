package com.backend.adiministror.model;

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
    private DocumentType documentType;

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
}
