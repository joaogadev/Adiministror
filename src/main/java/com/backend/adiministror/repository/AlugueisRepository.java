package com.backend.adiministror.repository;

import com.backend.adiministror.model.AluguelModel;
import com.backend.adiministror.model.enums.StatusAluguel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlugueisRepository extends JpaRepository<AluguelModel, UUID> {

    boolean existsBySala_IdAndStatus(UUID salaId, StatusAluguel status);

    Optional<AluguelModel> findBySala_Id(UUID salaId);

    Optional<AluguelModel> findByInquilino_Id(UUID inquilinoId);

    List<AluguelModel> findBySala_Galeria_Dono_IdAndStatus(UUID donoId, StatusAluguel status);

    boolean existsByInquilino_IdAndSala_Galeria_Dono_Id(UUID tenantId, UUID donoId);

    boolean existsByInquilino_IdAndStatusAndIdNot(UUID tenantId, StatusAluguel status, UUID aluguelId);

    Optional<AluguelModel> findBySala_IdAndStatus(UUID salaId, StatusAluguel status);

    List<AluguelModel> findByInquilino_IdAndSala_Galeria_Dono_Id(UUID tenantId, UUID donoId);
}
