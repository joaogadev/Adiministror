package com.backend.adiministror.repository;

import com.backend.adiministror.model.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, UUID> {
    boolean existsByEmail(String email);

    Optional<UsuarioModel> findByEmail(String email);
}
