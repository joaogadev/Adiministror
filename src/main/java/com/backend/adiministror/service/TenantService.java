package com.backend.adiministror.service;

import com.backend.adiministror.dto.response.TenantResponse;
import com.backend.adiministror.dto.request.TenantResquest;
import com.backend.adiministror.model.AluguelModel;
import com.backend.adiministror.model.enums.StatusAluguel;
import com.backend.adiministror.model.TenantModel;
import com.backend.adiministror.repository.AlugueisRepository;
import com.backend.adiministror.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;
    private final AlugueisRepository alugueisRepository;
    private final CurrentUserService currentUserService;

    public TenantModel buscarOuCriar(TenantResquest request) {

        String normalizedEmail = normalizedEmail(request.email());
        String normalizedPhone = normalizedPhone(request.phone());
        String normalizedDocumentNumber =
                normalizedDocumentNumber(request.documentNumber());

        var tenatExistente = tenantRepository.findByDocumentNumber(normalizedDocumentNumber);

        if (tenatExistente.isPresent()) {
            TenantModel tenant = tenatExistente.get();

            if (tenant.getEmail().equals(normalizedEmail) && tenantRepository.existsByEmail(normalizedEmail)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Já existe um perfil com esse email"
                );
            }

            tenant.atualizrDados(
                    request.name(),
                    normalizedEmail,
                    normalizedPhone
            );

            tenant.ativar();

            return tenantRepository.save(tenant);
        }

        TenantModel tenant = new TenantModel(
                request.name(),
                request.phone(),
                normalizedEmail,
                normalizedDocumentNumber,
                request.documentType()
        );

        tenant.ativar();

        return tenantRepository.save(tenant);
    }

    public TenantResponse update(String documentNumber, TenantResquest request) {
        String normalizedEmail = normalizedEmail(request.email());
        String normalizedPhone = normalizedPhone(request.phone());
        String normalizedDocumentNumber = normalizedDocumentNumber(documentNumber);

        TenantModel tenant = tenantRepository.findByDocumentNumber(normalizedDocumentNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant não encontrado"));

        validarAcessoTenant(tenant.getId());

        if (!tenant.getEmail().equalsIgnoreCase(normalizedEmail) && tenantRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Já existe um perfil com esse email"
            );
        }


        tenant.atualizrDados(
                request.name(),
                normalizedEmail,
                normalizedPhone
        );

        TenantModel updatedTenant = tenantRepository.save(tenant);

        return TenantResponse.from(updatedTenant);
    }

    public void desativar(UUID id, UUID aluguelId) {
        boolean aluguelAtivo = alugueisRepository.
                existsByInquilino_IdAndStatusAndIdNot(id, StatusAluguel.ATIVO, aluguelId);

        if (aluguelAtivo) {
            return;
        }

        TenantModel tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant não encontrado"));

        tenant.desativar();

        tenantRepository.save(tenant);
    }

    public TenantResponse buscar(UUID id) {
        TenantModel tenant = bucarTenantAutorizado(id);

        return TenantResponse.from(tenant);
    }

    public List<TenantResponse> buscarPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RuntimeException("Digite algo para buscar!");
        }

        if (currentUserService.isAdmin()) {
            return tenantRepository.findByNomeContainingIgnoreCase(nome.trim())
                    .stream()
                    .map(TenantResponse::from)
                    .toList();
        }

        UUID usuarioAtual = currentUserService.getCurrentUser().getId();

        return alugueisRepository
                .findBySala_Galeria_Dono_IdAndStatus(usuarioAtual, StatusAluguel.ATIVO)
                .stream()
                .map(AluguelModel::getInquilino)
                .filter(tenant -> tenant.getNome().toLowerCase(Locale.ROOT)
                        .contains(nome.trim().toLowerCase(Locale.ROOT)))
                .map(TenantResponse::from)
                .toList();

    }

    public TenantResponse buscarPorDocumentNumber(String documentNumber) {
        String normalizedDocumentNumber = normalizedDocumentNumber(documentNumber);

        TenantModel tenant = tenantRepository.findByDocumentNumber(normalizedDocumentNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant não encontrado"));

        validarAcessoTenant(tenant.getId());

        return TenantResponse.from(tenant);
    }

    public TenantResponse buscarPorEmail(String email) {
        String normalizedEmail = normalizedEmail(email);

        TenantModel tenant = tenantRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant não encontrado"));

        validarAcessoTenant(tenant.getId());

        return TenantResponse.from(tenant);
    }

    public List<TenantResponse> buscarTodos() {
        if (currentUserService.isAdmin()) {
            return tenantRepository.findByAtivoTrue()
                    .stream()
                    .map(TenantResponse::from)
                    .toList();
        }

        UUID usuarioAtual = currentUserService.getCurrentUser().getId();

        return alugueisRepository
                .findBySala_Galeria_Dono_IdAndStatus(usuarioAtual, StatusAluguel.ATIVO)
                .stream()
                .map(AluguelModel::getInquilino)
                .map(TenantResponse::from)
                .toList();

    }

    private TenantModel bucarTenantAutorizado(UUID id) {
        TenantModel tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant não encontrado"));

        validarAcessoTenant(id);

        return tenant;
    }

    private void validarAcessoTenant(UUID tenantId) {

        if (currentUserService.isAdmin()) {
            return;
        }

        UUID usuarioAtual =
                currentUserService.getCurrentUserId();

        boolean possuiAcesso = alugueisRepository
                .existsByInquilino_IdAndSala_Galeria_Dono_Id(tenantId, usuarioAtual);

        if (!possuiAcesso) {
            throw new RuntimeException(
                    "Você não tem permissão para acessar este tenant"
            );
        }
    }

    private String normalizedEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email não pode ser vazio");
        }

        return email.toLowerCase(Locale.ROOT).trim();
    }

    private String normalizedPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return phone.trim();
    }

    private String normalizedDocumentNumber(String documentNumber) {
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Número de documento não pode ser vazio");
        }
        return documentNumber.replaceAll("\\D", "").trim();
    }
}
