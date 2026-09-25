package com.backend.adiministror.repository;

import com.backend.adiministror.model.PagamentoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PagamentoRepository extends JpaRepository<PagamentoModel, UUID> {
    boolean existsByAluguel_IdAndCompetencia(
            UUID aluguelId,
            LocalDate competencia
    );

    List<PagamentoModel> findByAluguel_IdOrderByCompetenciaDesc(
            UUID aluguelId
    );

    List<PagamentoModel> findByAluguel_Sala_Galeria_Dono_Id(
            UUID donoId
    );
}
