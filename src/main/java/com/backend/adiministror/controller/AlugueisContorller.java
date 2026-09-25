package com.backend.adiministror.controller;

import com.backend.adiministror.dto.request.AluguelRequest;
import com.backend.adiministror.dto.request.AluguelUpdateRequest;
import com.backend.adiministror.dto.response.AluguelResponse;
import com.backend.adiministror.service.AlugueisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/alugueis")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DONO')")
public class AlugueisContorller {
    private final AlugueisService alugueisService;

    @PostMapping("/sala/{id}")
    public ResponseEntity<AluguelResponse> create(@PathVariable UUID id, @Valid @RequestBody AluguelRequest aluguelRequest) {
        AluguelResponse  aluguelResponse = alugueisService.create(id, aluguelRequest);

        return ResponseEntity.ok().body(aluguelResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AluguelResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(alugueisService.buscar(id));
    }

    @GetMapping("/sala/{salaId}")
    public ResponseEntity<AluguelResponse> buscarPorSala(@PathVariable UUID salaId) {
        return ResponseEntity.ok(alugueisService.buscarPorSala(salaId));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<AluguelResponse>>buscarPorTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(alugueisService.buscarPorTenant(tenantId));
    }

    @GetMapping
    public ResponseEntity<List<AluguelResponse>> buscarTodos() {
        return ResponseEntity.ok(alugueisService.buscarTodos());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AluguelResponse> update(@PathVariable UUID id, @Valid @RequestBody AluguelUpdateRequest aluguelUpdateRequest) {
        return ResponseEntity.ok(alugueisService.update(id, aluguelUpdateRequest));
    }

    @PatchMapping("/{id}/encerrar")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        alugueisService.encerrar(id);
        return ResponseEntity.noContent().build();
    }
}
