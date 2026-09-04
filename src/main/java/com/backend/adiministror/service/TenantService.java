package com.backend.adiministror.service;

import com.backend.adiministror.dto.response.TenantResponse;
import com.backend.adiministror.dto.request.TenantResquest;
import com.backend.adiministror.model.AluguelModel;
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

    public TenantModel saveTenant(TenantResquest request) {

        String normalizedEmail = normalizedEmail(request.email());
        String normalizedPhone = normalizedPhone(request.phone());
        String normalizedDocumentNumber =
                normalizedDocumentNumber(request.documentNumber());

        if (tenantRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Email já cadastrado");
        }

        if (tenantRepository.existsByDocumentNumber(normalizedDocumentNumber)) {
            throw new RuntimeException("Documento já cadastrado");
        }

        TenantModel tenant = new TenantModel(
                request.name(),
                normalizedEmail,
                normalizedPhone,
                normalizedDocumentNumber,
                request.documentType()
        );

        return tenantRepository.save(tenant);
    }

    public TenantResponse cretate(TenantResquest request) {
        TenantModel tenant = saveTenant(request);

        return TenantResponse.from(tenant);
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

    private void delete(UUID id) {
        TenantModel tenant = tenantRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant não encontrado"));

        tenantRepository.delete(tenant);
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
                .findBySala_Galeria_Dono_Id(usuarioAtual)
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
            return tenantRepository.findAll()
                    .stream()
                    .map(TenantResponse::from)
                    .toList();
        }

        UUID usuarioAtual = currentUserService.getCurrentUser().getId();

        return alugueisRepository
                .findBySala_Galeria_Dono_Id(usuarioAtual)
                .stream()
                .map(AluguelModel::getInquilino)
                .map(TenantResponse::from)
                .toList();

    }

    private TenantModel bucarTenantAutorizado(UUID id) {
        TenantModel tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant não encontrado"));

        if (!currentUserService.isAdmin()) {
            return tenant;
        }

        AluguelModel aluguel = alugueisRepository.findByInquilino_Id(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluguel não encontrado"));

        UUID usuarioAtual = currentUserService.getCurrentUser().getId();

        UUID dono = aluguel.getSala().getGaleria().getDono().getId();

        if (!dono.equals(usuarioAtual)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este tenant");
        }

        return tenant;
    }

    private void validarAcessoTenant(UUID tenantId) {

        if (currentUserService.isAdmin()) {
            return;
        }

        AluguelModel aluguel =
                alugueisRepository
                        .findByInquilino_Id(tenantId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Aluguel não encontrado"
                                ));

        UUID usuarioAtual =
                currentUserService.getCurrentUserId();

        UUID dono =
                aluguel.getSala()
                        .getGaleria()
                        .getDono()
                        .getId();

        if (!dono.equals(usuarioAtual)) {
            throw new RuntimeException(
                    "Você não tem permissão para alterar este tenant"
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
