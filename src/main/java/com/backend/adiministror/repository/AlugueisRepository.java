package com.backend.adiministror.repository;

import com.backend.adiministror.model.AluguelModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlugueisRepository extends JpaRepository<AluguelModel, UUID> {
}
