package com.backend.adiministror.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "endereco")
public class EnderecoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "zip_code", nullable = false, length = 8)
    private String zipCode;

    @Column(name = "estado", length = 2)
    private String estado;

    @Column(name = "cidade", length = 255)
    private String cidade;

    @Column(name = "bairro", length = 255)
    private String bairro;

    @Column(name = "rua", length = 255)
    private String rua;

    @Column(name = "numero", length = 10)
    private String numero;

    @Column(name = "complemento", length = 255)
    private String complemento;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public EnderecoModel(String zipCode,
                        String estado,
                        String cidade,
                        String bairro,
                        String rua,
                        String numero,
                        String complemento
    ) {
        this.zipCode = zipCode;
        this.estado = estado;
        this.cidade = cidade;
        this.bairro = bairro;
        this.rua = rua;
        this.numero = numero;
        this.complemento = complemento;
    }
}
