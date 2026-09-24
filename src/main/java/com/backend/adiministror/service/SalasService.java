package com.backend.adiministror.service;

import com.backend.adiministror.dto.request.SalasRequest;
import com.backend.adiministror.dto.response.SalasResponse;
import com.backend.adiministror.exception.BusinessValidationException;
import com.backend.adiministror.exception.ForbidenException;
import com.backend.adiministror.exception.ResourceNotFoundException;
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
    private final CurrentUserService currentUserService;
    private final GaleriaRepository galeriaRepository;

    public SalasResponse create(UUID galediaId, SalasRequest request) {
        GaleriaModel galeriaModel = galeriaRepository.findById(galediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Galeria não encontrada"));

        if (request.nome() == null || request.nome().trim().isEmpty()) {
            throw new BusinessValidationException("Nome da sala não pode ser vazio");
        }

        if (!currentUserService.isAdmin()) {
            UUID usuarioAtual = currentUserService.getCurrentUser().getId();

            if (!galeriaModel.getDono().getId().equals(usuarioAtual)) {
                throw new ForbidenException("Você não tem permissão para criar uma sala nesta galeria");
            }
        }

        SalasModel sala = new SalasModel(
                request.nome().trim(),
                galeriaModel
        );

        SalasModel savedSalasModel = salasRepository.save(sala);

        return SalasResponse.from(savedSalasModel);
    }

    public SalasResponse update(UUID id, SalasRequest request) {
        SalasModel salasModel = buscarSalasAutorizadas(id);

        salasModel.atualizarDados(request.nome());

        SalasModel newSalasModel = salasRepository.save(salasModel);

        return SalasResponse.from(newSalasModel);
    }

    public void delete(UUID id) {
        SalasModel sala = buscarSalasAutorizadas(id);

        salasRepository.delete(sala);
    }

    public SalasResponse buscar(UUID id) {
        return SalasResponse.from(buscarSalasAutorizadas(id));
    }

    public List<SalasResponse> buscarPorGaleria(UUID galeriaId) {
        GaleriaModel galeria = galeriaRepository.findById(galeriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Galeria não encontrada"));

        if (!currentUserService.isAdmin()) {
            UUID usuarioAtual = currentUserService.getCurrentUser().getId();

            if (!galeria.getDono().getId().equals(usuarioAtual)) {
                throw new ForbidenException("Você não tem permissão para acessar esta galeria");
            }
        }

        return salasRepository.findByGaleriaId(galeriaId).stream()
                .map(SalasResponse::from)
                .toList();
    }

    private SalasResponse buscarSalaDaGaleria(UUID id, UUID galeriaId) {
        return salasRepository
                .findByIdAndGaleriaId(id, galeriaId)
                .map(SalasResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Sala não encontrada"));

    }

    private SalasModel buscarSalasAutorizadas(UUID id) {
        SalasModel sala = salasRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sala não encontrada"));

        if (currentUserService.isAdmin()) {
            return sala;
        }

        UUID usuarioAtual = currentUserService.getCurrentUserId();

        UUID donoGaleria = sala.getGaleria().getDono().getId();

        if (!donoGaleria.equals(usuarioAtual)) {
            throw new ForbidenException("Você não tem permissão para acessar esta sala");
        }

        return sala;
    }

    public List<SalasResponse> buscarPorNome(String nome) {
        if(currentUserService.isAdmin()) {
            return salasRepository
                    .findByNomeContainingIgnoreCase(nome.trim())
                    .stream()
                    .map(SalasResponse::from)
                    .toList();
        }
        if (nome == null || nome.trim().isEmpty()) {
            throw new BusinessValidationException("Digite algo para buscar!");
        }
        return salasRepository
                .findByNomeContainingIgnoreCaseGaleria_Dono_Id(nome.trim(), currentUserService.getCurrentUserId())
                .stream()
                .map(SalasResponse::from)
                .toList();
    }
}
