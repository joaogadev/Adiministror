package com.backend.adiministror.service;

import com.backend.adiministror.dto.request.AluguelRequest;
import com.backend.adiministror.dto.request.AluguelUpdateRequest;
import com.backend.adiministror.dto.response.AluguelResponse;
import com.backend.adiministror.model.AluguelModel;
import com.backend.adiministror.model.SalasModel;
import com.backend.adiministror.model.enums.StatusAluguel;
import com.backend.adiministror.model.TenantModel;
import com.backend.adiministror.repository.AlugueisRepository;
import com.backend.adiministror.repository.SalasRepository;
import com.backend.adiministror.repository.TenantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlugueisService {
    private final AlugueisRepository alugueisRepository;
    private final SalasRepository salasRepository;
    private final TenantRepository tenantRepository;
    private final TenantService tenantService;
    private final CurrentUserService currentUserService;

    @Transactional
    public AluguelResponse create(UUID id, AluguelRequest request) {

        SalasModel sala = salasRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        if (!currentUserService.isAdmin()) {
            UUID usuarioAtual = currentUserService.getCurrentUser().getId();

            if (!sala.getGaleria().getDono().getId().equals(usuarioAtual)) {
                throw new RuntimeException("Você não tem permissão para alugar esta sala");
            }
        }

        boolean possuiAluguelAtivo = alugueisRepository
                .existsBySala_IdAndStatus(id, StatusAluguel.ATIVO);

        if (possuiAluguelAtivo) {
            throw new RuntimeException("Sala já está alugada");
        }

        TenantModel tenantSalvo = tenantService.buscarOuCriar(request.inquilino());

        AluguelModel aluguel = new AluguelModel(
                sala,
                tenantSalvo,
                LocalDate.now(),
                request.diaVencimentoPadrao(),
                request.valorAluguel(),
                StatusAluguel.ATIVO
        );

        AluguelModel savedAluguel = alugueisRepository.save(aluguel);

        return AluguelResponse.from(savedAluguel);
    }

    public AluguelResponse buscar(UUID id) {
        AluguelModel aluguel = buscarAlugueisAutorizado(id);

        return AluguelResponse.from(aluguel);
    }

    public AluguelResponse buscarPorSala(UUID salaId) {
        AluguelModel alugel = alugueisRepository
                .findBySala_IdAndStatus(salaId, StatusAluguel.ATIVO)
                .orElseThrow(() -> new RuntimeException("Sala não possui aluguel ativo"));

        return AluguelResponse.from(validarAcesso(alugel));
    }

    public List<AluguelResponse> buscarPorTenant(UUID tenantId) {

        if (!currentUserService.isAdmin()) {
            return alugueisRepository
                    .findByInquilino_Id(tenantId)
                    .stream()
                    .map(AluguelResponse::from)
                    .toList();
        }

        UUID usuarioAtual = currentUserService.getCurrentUserId();

        return alugueisRepository.findByInquilino_IdAndSala_Galeria_Dono_Id(tenantId, usuarioAtual)
                .stream()
                .filter(aluguel -> aluguel.getSala().getGaleria().getDono().getId().equals(usuarioAtual))
                .map(AluguelResponse::from)
                .toList();
    }

    public List<AluguelResponse> buscarTodos() {
        if (currentUserService.isAdmin()) {
            return alugueisRepository.findAll()
                    .stream()
                    .map(AluguelResponse::from)
                    .toList();
        }

        UUID usuarioAtual = currentUserService.getCurrentUserId();

        return alugueisRepository.findBySala_Galeria_Dono_IdAndStatus(usuarioAtual, StatusAluguel.ATIVO)
                .stream()
                .map(AluguelResponse::from)
                .toList();
    }

    public AluguelResponse update(UUID id, AluguelUpdateRequest request) {
        AluguelModel aluguel = buscarAlugueisAutorizado(id);

        aluguel.atualizarDados(
            request.diaVencimentoPadrao(),
            request.dataInicio(),
            request.valorAluguel()
        );

        return AluguelResponse.from(alugueisRepository.save(aluguel));
    }

    private AluguelModel buscarAlugueisAutorizado(UUID alugueisId) {
        AluguelModel aluguel = alugueisRepository.findById(alugueisId)
                .orElseThrow(() -> new RuntimeException("Aluguel não encontrado"));

        return validarAcesso(aluguel);
    }

    private AluguelModel validarAcesso(AluguelModel aluguel) {
        if (currentUserService.isAdmin()) {
            return aluguel;
        }

        UUID usuarioAtual =
                currentUserService.getCurrentUserId();

        UUID dono =
                aluguel.getSala()
                        .getGaleria()
                        .getDono()
                        .getId();

        if (!dono.equals(usuarioAtual)) {
            throw new RuntimeException(
                    "Você não tem permissão para acessar este aluguel"
            );
        }

        return aluguel;
    }

    @Transactional
    public void encerrar(UUID id) {
        AluguelModel aluguelModel = buscarAlugueisAutorizado(id);

        if (aluguelModel.getStatus() == StatusAluguel.ENCERRADO) {
            throw new RuntimeException("Aluguel já está encerrado");
        }

        TenantModel tenantModel = aluguelModel.getInquilino();

        aluguelModel.encerrar();

        boolean possuiOutrosAlugueisAtivos = alugueisRepository.
                existsByInquilino_IdAndStatusAndIdNot(
                        tenantModel.getId(),
                        StatusAluguel.ATIVO,
                        aluguelModel.getId()
                );

        if (!possuiOutrosAlugueisAtivos) {
            tenantModel.desativar();
            tenantRepository.delete(tenantModel);
        }

        alugueisRepository.delete(aluguelModel);
    }
}
