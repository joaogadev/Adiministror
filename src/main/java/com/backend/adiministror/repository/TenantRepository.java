package com.backend.adiministror.repository;

import com.backend.adiministror.model.TenantModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<TenantModel, UUID> {
    boolean existsByEmail(String email);

    Optional<TenantModel> findByEmail(String email);

    boolean existsByDocumentNumber(String documentNumber);

    Optional<TenantModel> findByDocumentNumber(String documentNumber);

    List<TenantModel> findByNomeContainingIgnoreCase(String nome);
}
