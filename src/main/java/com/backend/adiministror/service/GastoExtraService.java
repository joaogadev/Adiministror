package com.backend.adiministror.service;

import com.backend.adiministror.dto.request.GastoExtraRequest;
import com.backend.adiministror.dto.response.GastoExtraResponse;
import com.backend.adiministror.exception.BusinessValidationException;
import com.backend.adiministror.exception.ForbidenException;
import com.backend.adiministror.exception.ResourceNotFoundException;
import com.backend.adiministror.model.GaleriaModel;
import com.backend.adiministror.model.GastoExtraModel;
import com.backend.adiministror.repository.GaleriaRepository;
import com.backend.adiministror.repository.GastosExtrasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GastoExtraService {
    private final CurrentUserService currentUserService;
    private final GastosExtrasRepository gastosExtrasRepository;
    private final GaleriaRepository galeriaRepository;

    public GastoExtraResponse createGastoExtra(UUID id, GastoExtraRequest gastoExtraRequest) {
        GaleriaModel galeriaModel = buscarGaleriaAutorizada(id);

        GastoExtraModel gastoExtra = new GastoExtraModel(
                gastoExtraRequest.nome(),
                gastoExtraRequest.descricao(),
                gastoExtraRequest.valor(),
                gastoExtraRequest.dataGasto(),
                galeriaModel
        );

        GastoExtraModel gastoExtraSalvo = gastosExtrasRepository.save(gastoExtra);

        return GastoExtraResponse.from(gastoExtraSalvo);
    }

    public GastoExtraResponse updateGastoExtra(UUID id, GastoExtraRequest gastoExtraRequest) {
        GastoExtraModel gastoExtraModel = buscarGastoExtraAutorizado(id);

        gastoExtraModel.atualizarDados(
                gastoExtraRequest.nome(),
                gastoExtraRequest.descricao(),
                gastoExtraRequest.valor(),
                gastoExtraRequest.dataGasto()
        );

        GastoExtraModel newGastoExtraModel = gastosExtrasRepository.save(gastoExtraModel);

        return GastoExtraResponse.from(newGastoExtraModel);
    }

    public GastoExtraResponse buscar(UUID id) {
        return GastoExtraResponse.from(buscarGastoExtraAutorizado(id));
    }

    public List<GastoExtraResponse> buscarGastosExtras(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new BusinessValidationException("Digite algo para buscar!");
        }
        String nomeNormalizado = nome.trim();

        if (currentUserService.isAdmin()) {
            return gastosExtrasRepository.findByNomeContainingIgnoreCase(nomeNormalizado)
                    .stream().map(GastoExtraResponse::from).toList();
        }

        return gastosExtrasRepository.findByNomeContainingIgnoreCaseAndGaleria_Dono_Id(nomeNormalizado, currentUserService.getCurrentUserId())
                .stream()
                .map(GastoExtraResponse::from)
                .toList();
    }

    public void deleteGastoExtra(UUID id) {
        GastoExtraModel gastoExtraModel = buscarGastoExtraAutorizado(id);
        gastosExtrasRepository.delete(gastoExtraModel);
    }

    public List<GastoExtraResponse> buscarGastosExtrasPorGaleria(UUID galeriaId) {
        buscarGaleriaAutorizada(galeriaId);

        return gastosExtrasRepository.findByGaleriaId(galeriaId)
                .stream()
                .map(GastoExtraResponse::from)
                .toList();
    }

    public List<GastoExtraResponse> buscarGastosExtrasPorPeriodo(UUID id, LocalDate dataInicio, LocalDate dataFim) {
        buscarGaleriaAutorizada(id);

        if (dataFim == null || dataInicio == null) {
            throw new BusinessValidationException("Data de início e fim são obrigatórias");
        }

        if (dataFim.isBefore(dataInicio)) {
            throw new BusinessValidationException("Data de fim não pode ser anterior à data de início");
        }

        return gastosExtrasRepository.findByGaleria_IdAndDataGastoBetween(id, dataInicio, dataFim)
                .stream()
                .map(GastoExtraResponse::from)
                .toList();
    }

    private GastoExtraModel buscarGastoExtraAutorizado(UUID gastosExtraId) {
        GastoExtraModel gastoExtraModel = gastosExtrasRepository.findById(gastosExtraId)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto extra não encontrado"));

        return validarAcesso(gastoExtraModel);
    }

    private GaleriaModel buscarGaleriaAutorizada(UUID galeriaId) {
        GaleriaModel galeriaModel = galeriaRepository.findById(galeriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Galeria não encontrada"));

        if (currentUserService.isAdmin()) {
            return galeriaModel;
        }

        UUID usuarioAtual = currentUserService.getCurrentUserId();

        if (!galeriaModel.getDono().getId().equals(usuarioAtual)) {
            throw new ForbidenException("Você não tem permissão para acessar esta galeria");
        }

        return galeriaModel;
    }

    private GastoExtraModel validarAcesso(GastoExtraModel gastoExtraModel) {
        if (currentUserService.isAdmin()) {
            return gastoExtraModel;
        }

        UUID usuarioAtual =
                currentUserService.getCurrentUserId();

        UUID dono =
                gastoExtraModel.getGaleria().getDono().getId();


        if (!dono.equals(usuarioAtual)) {
            throw new ForbidenException(
                    "Você não tem permissão para acessar este gasto"
            );
        }

        return gastoExtraModel;
    }
}
