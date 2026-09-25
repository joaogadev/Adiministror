package com.backend.adiministror.repository;

import com.backend.adiministror.model.ContratoModel;
import com.backend.adiministror.model.enums.StatusContrato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContratoRepository extends JpaRepository<ContratoModel, UUID> {

    //serve para garantir que um aluguel não possa possuir dois contratos ativos ao mesmo tempo
    boolean existsByAluguel_IdAndStatus(UUID aluguelId, StatusContrato status);

    Optional<ContratoModel> findByAluguel_IdAndStatus(UUID aluguelId, StatusContrato status);

    List<ContratoModel> findByAluguel_IdOrderByDataInicioDesc(UUID galeriaId);

    List<ContratoModel> findByAluguel_Sala_Galeria_Dono_Id(UUID donoId);
}
