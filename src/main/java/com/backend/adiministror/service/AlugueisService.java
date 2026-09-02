package com.backend.adiministror.service;

import com.backend.adiministror.dto.request.AluguelRequest;
import com.backend.adiministror.dto.request.AluguelUpdateRequest;
import com.backend.adiministror.dto.response.AluguelResponse;
import com.backend.adiministror.model.AluguelModel;
import com.backend.adiministror.model.SalasModel;
import com.backend.adiministror.model.TenantModel;
import com.backend.adiministror.repository.AlugueisRepository;
import com.backend.adiministror.repository.SalasRepository;
import com.backend.adiministror.repository.TenantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlugueisService {
    private final AlugueisRepository alugueisRepository;
    private final SalasRepository salasRepository;
    private final TenantRepository tenantRepository;
    public final TenantService tenantService;
    private final CurrentUserService currentUserService;

    @Transactional
    public AluguelResponse create(UUID id, AluguelRequest request) {

        SalasModel sala = salasRepository.findById(id).orElseThrow(() -> new RuntimeException("Sala não encontrada"));

        if (alugueisRepository.existsBySala_Id(id)) {
            throw new RuntimeException("Sala já está alugada");
        }

        if (request.dataVencimento().isBefore(request.dataInicio())) {
            throw new RuntimeException("Data de vencimento não pode ser anterior à data de início");
        }

        if (!currentUserService.isAdmin()) {
            UUID usuarioAtual = currentUserService.getCurrentUser().getId();

            if (!sala.getGaleria().getDono().getId().equals(usuarioAtual)) {
                throw new RuntimeException("Você não tem permissão para alugar esta sala");
            }
        }

        TenantModel tenantModel = new TenantModel(
                request.inquilino().name(),
                request.inquilino().email(),
                request.inquilino().phone(),
                request.inquilino().documentNumber(),
                request.inquilino().documentType()
        );

        TenantModel tenantSalvo = tenantService.saveTenant(request.inquilino());

        AluguelModel aluguel =  new AluguelModel(
                sala,
                tenantSalvo,
                request.dataInicio(),
                request.dataVencimento(),
                request.status()
        );

        AluguelModel savedAluguel = alugueisRepository.save(aluguel);

        return AluguelResponse.from(savedAluguel);
    }

    public AluguelResponse buscar(UUID id) {
        AluguelModel aluguel = alugueisRepository.findById(id).orElseThrow(() -> new RuntimeException("Aluguel não encontrado"));

        return AluguelResponse.from(aluguel);
    }

    public AluguelResponse buscarPorSala(UUID salaId) {
        return AluguelResponse.from(alugueisRepository.findBySala_Id(salaId).orElseThrow(() -> new RuntimeException("Aluguel não encontrado")));
    }

    public AluguelResponse buscarPorTenant(UUID tenantId) {

        AluguelModel aluguel = alugueisRepository
                .findByInquilino_Id(tenantId)
                .orElseThrow(() -> new RuntimeException("Aluguel não encontrado"));

        return AluguelResponse.from(aluguel);
    }

    public List<AluguelResponse> buscarTodos() {
        return alugueisRepository.findAll()
                .stream()
                .map(AluguelResponse::from)
                .toList();
    }

    public AluguelResponse update(UUID id, AluguelUpdateRequest request) {
        AluguelModel aluguel = buscarAlugueisAutorizado(id);

        if (request.dataVencimento().isBefore(request.dataInicio())) {
            throw new RuntimeException("Data de vencimento não pode ser anterior à data de início");
        }

        aluguel.atualizarDados(
            request.dataVencimento(),
            request.dataInicio(),
            request.status()
        );

        return AluguelResponse.from(alugueisRepository.save(aluguel));
    }

    private AluguelModel buscarAlugueisAutorizado(UUID galeriaId) {
        AluguelModel aluguel = alugueisRepository.findById(galeriaId)
                .orElseThrow(() -> new RuntimeException("Aluguel não encontrado"));

        if (currentUserService.isAdmin()) {
            return aluguel;
        }

        UUID usuarioAtual = currentUserService.getCurrentUserId();

        UUID dono = aluguel.getSala().getGaleria().getDono().getId();

        if (!dono.equals(usuarioAtual)) {
            throw new RuntimeException("Você não tem permissão para acessar este aluguel");
        }
        return aluguel;
    }

    @Transactional
    public void encerrar(UUID id) {
        AluguelModel aluguelModel = buscarAlugueisAutorizado(id);

        TenantModel tenantModel = aluguelModel.getInquilino();

        alugueisRepository.delete(aluguelModel);
        tenantRepository.delete(tenantModel);
    }
}
