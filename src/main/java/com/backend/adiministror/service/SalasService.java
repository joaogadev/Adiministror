package com.backend.adiministror.service;

import com.backend.adiministror.dto.SalasRequest;
import com.backend.adiministror.dto.SalasResponse;
import com.backend.adiministror.model.GaleriaModel;
import com.backend.adiministror.model.SalasModel;
import com.backend.adiministror.repository.GaleriaRepository;
import com.backend.adiministror.repository.SalasRepository;
import com.backend.adiministror.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SalasService {
    private final SalasRepository salasRepository;
    private UsuarioRepository usuarioRepository;
    private SalasModel salasModel;
    private final GaleriaRepository galeriaRepository;

    public SalasResponse create(UUID galediaId, SalasRequest request) {
        GaleriaModel galeriaModel = galeriaRepository.findById(galediaId)
                .orElseThrow(() -> new RuntimeException("Galeria não encontrada"));

        if (request.nome() == null || request.nome().trim().isEmpty()) {
            throw new RuntimeException("Nome da sala não pode ser vazio");
        }

        SalasModel sala = new SalasModel(
                request.nome().trim(),
                galeriaModel
        );

        SalasModel savedSalasModel = salasRepository.save(sala);

        return SalasResponse.from(savedSalasModel);
    }

    public SalasResponse update(UUID id, SalasRequest request) {
        SalasModel salasModel = salasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        salasModel.atualizarDados(request.nome());

        SalasModel newSalasModel = salasRepository.save(salasModel);

        return SalasResponse.from(newSalasModel);
    }

    public void delete(UUID id) {
        SalasModel sala = salasRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Sala não encontrada"));

        salasRepository.delete(sala);
    }

    public SalasModel buscar(UUID id) {
        return salasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));
    }

    public List<SalasResponse> buscarPorGaleria(UUID galeriaId) {
        return salasRepository.findByGaleriaId(galeriaId).stream()
                .map(SalasResponse::from)
                .toList();
    }

    private SalasResponse buscarSalaDaGaleria(UUID id, UUID galeriaId) {
        return salasRepository
                .findByIdAndGaleriaId(id, galeriaId)
                .map(SalasResponse::from)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

    }

    public List<SalasResponse> buscarPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RuntimeException("Digite algo para buscar!");
        }
        return salasRepository.findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(SalasResponse::from)
                .toList();
    }
}
