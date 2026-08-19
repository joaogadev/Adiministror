package com.backend.adiministror.repository;

import com.backend.adiministror.model.GaleriaModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GaleriaRepository extends JpaRepository<GaleriaModel, UUID> {
    List<GaleriaModel> findByNomeContainingIgnoreCase(String nome);

    List<GaleriaModel> findByDono_Id(UUID id);

    List<GaleriaModel> findByEnderecoCidadeIgnoreCase(String cidade);

    //boolean existsByDonoIdAndNomeIgnoreCase(UUID donoId, String nome); //para evitar que o mesmo usuário crie galerias com o mesmo nome
}
