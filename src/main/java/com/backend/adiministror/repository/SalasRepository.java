package com.backend.adiministror.repository;

import com.backend.adiministror.model.SalasModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SalasRepository extends JpaRepository<SalasModel, UUID> {
    boolean existsById(UUID id);

    Optional<SalasModel> findById(UUID id);
}
