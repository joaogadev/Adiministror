package com.backend.adiministror.service;

import com.backend.adiministror.dto.response.TenantResponse;
import com.backend.adiministror.dto.request.TenantResquest;
import com.backend.adiministror.model.TenantModel;
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

    private TenantResponse buscar(UUID id) {
        return tenantRepository.findById(id)
                .map(TenantResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant não encontrado"));
    }

    public List<TenantResponse> buscarPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new RuntimeException("Digite algo para buscar!");
        }
        return tenantRepository.findByNomeContainingIgnoreCase(nome.trim())
                .stream()
                .map(TenantResponse::from)
                .toList();
    }

    public TenantResponse buscarPorDocumentNumber(String documentNumber) {
        String normalizedDocumentNumber = normalizedDocumentNumber(documentNumber);

        TenantModel tenant = tenantRepository.findByDocumentNumber(normalizedDocumentNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant não encontrado"));

        return TenantResponse.from(tenant);
    }

    public TenantResponse buscarPorEmail(String email) {
        String normalizedEmail = normalizedEmail(email);

        TenantModel tenant = tenantRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant não encontrado"));

        return TenantResponse.from(tenant);
    }

    public List<TenantResponse> buscarTodos() {
        return tenantRepository.findAll()
                .stream()
                .map(TenantResponse::from)
                .toList();
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
