package com.backend.adiministror.repository;

import com.backend.adiministror.model.GaleriaModel;
import com.backend.adiministror.model.SalasModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalasRepository extends JpaRepository<SalasModel, UUID> {
    boolean existsById(UUID id);

    boolean existsByGaleriaId(UUID id);

    Optional<SalasModel> findById(UUID id);

    Optional<SalasModel> findByIdAndGaleriaId(UUID id, UUID galeriaId);

    long countByGaleriaId(UUID id);

    List<SalasModel> findByGaleriaId(UUID id);

    List<SalasModel> findByNomeContainingIgnoreCase(String nome);
}
