package com.backend.adiministror.repository;

import com.backend.adiministror.model.AluguelModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlugueisRepository extends JpaRepository<AluguelModel, UUID> {

    boolean existsBySala_Id(UUID salaId);

    Optional<AluguelModel> findBySala_Id(UUID salaId);

    Optional<AluguelModel> findByInquilino_Id(UUID inquilinoId);
}
