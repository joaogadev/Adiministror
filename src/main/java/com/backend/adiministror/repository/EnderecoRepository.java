package com.backend.adiministror.repository;

import com.backend.adiministror.model.EnderecoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EnderecoRepository extends JpaRepository<EnderecoModel, UUID> {
}
