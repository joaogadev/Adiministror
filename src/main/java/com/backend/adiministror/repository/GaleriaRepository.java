package com.backend.adiministror.repository;

import com.backend.adiministror.model.GaleriaModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GaleriaRepository extends JpaRepository<GaleriaModel, UUID> {
    List<GaleriaModel> findByNomeIgnoreCase(String nome);
    long countBySalaId(UUID salaId);
}
