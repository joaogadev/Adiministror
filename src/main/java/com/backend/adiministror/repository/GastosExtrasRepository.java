package com.backend.adiministror.repository;

import com.backend.adiministror.model.GastoExtraModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GastosExtrasRepository extends JpaRepository<GastoExtraModel, UUID> {
    List<GastoExtraModel> findByNomeContainingIgnoreCaseAndGaleria_Dono_Id(String nome, UUID donoId);

    List<GastoExtraModel> findByNomeContainingIgnoreCase(String nome);

    List<GastoExtraModel> findByGaleriaId(UUID galeriaId);

    List<GastoExtraModel> findByGaleria_IdAndDataGastoBetween(UUID galeriaId, LocalDate dataInicio, LocalDate dataFim);
}
