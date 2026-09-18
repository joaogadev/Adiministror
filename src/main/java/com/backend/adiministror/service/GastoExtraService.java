package com.backend.adiministror.service;

import com.backend.adiministror.dto.request.GastoExtraReqeust;
import com.backend.adiministror.dto.response.GastoExtraResponse;
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

    public GastoExtraResponse createGastoExtra(UUID id, GastoExtraReqeust gastoExtraRequest) {
        GaleriaModel galeriaModel = galeriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Galeria não encontrada"));

        if (!currentUserService.isAdmin()) {
            UUID usuarioAtual = currentUserService.getCurrentUserId();

            if (!galeriaModel.getDono().getId().equals(usuarioAtual)) {
                throw new RuntimeException("Você não tem permissão para adicionar gastos nesta galeria");
            }
        }

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

    public GastoExtraResponse updateGastoExtra(UUID id, GastoExtraReqeust gastoExtraRequest) {
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

    public List<GastoExtraResponse> buscarGastosExtras(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RuntimeException("Digite algo para buscar!");
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
        GaleriaModel galeriaModel = galeriaRepository.findById(galeriaId)
                .orElseThrow(() -> new RuntimeException("Galeria não encontrada"));

        if (!currentUserService.isAdmin()) {
            UUID usuarioAtual = currentUserService.getCurrentUserId();

            if (!galeriaModel.getDono().getId().equals(usuarioAtual)) {
                throw new RuntimeException("Você não tem permissão para acessar os gastos desta galeria");
            }
        }

        return gastosExtrasRepository.findByGaleriaId(galeriaId)
                .stream()
                .map(GastoExtraResponse::from)
                .toList();
    }

    public List<GastoExtraResponse> buscarGastosExtrasPorPeriodo(UUID id, LocalDate dataInicio, LocalDate dataFim) {
        GaleriaModel galeriaModel = galeriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Galeria não encontrada"));

        if (!currentUserService.isAdmin()) {
            UUID usuarioAtual = currentUserService.getCurrentUserId();

            if (!galeriaModel.getDono().getId().equals(usuarioAtual)) {
                throw new RuntimeException("Você não tem permissão para acessar os gastos desta galeria");
            }
        }

        return gastosExtrasRepository.findByGaleria_IdAndDataGastoBetween(id, dataInicio, dataFim)
                .stream()
                .map(GastoExtraResponse::from)
                .toList();
    }

    private GastoExtraModel buscarGastoExtraAutorizado(UUID gastosExtraId) {
        GastoExtraModel gastoExtraModel = gastosExtrasRepository.findById(gastosExtraId)
                .orElseThrow(() -> new RuntimeException("Gasto extra não encontrado"));

        return validarAcesso(gastoExtraModel);
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
            throw new RuntimeException(
                    "Você não tem permissão para acessar este gasto"
            );
        }

        return gastoExtraModel;
    }
}
