package com.backend.adiministror.controller;

import com.backend.adiministror.dto.request.TenantResquest;
import com.backend.adiministror.dto.response.TenantResponse;
import com.backend.adiministror.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DONO')")
public class TenantController {

    private final TenantService tenantService;

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.buscar(id));
    }

    @GetMapping
    public ResponseEntity<List<TenantResponse>> buscarTodos() {
        return ResponseEntity.ok(tenantService.buscarTodos());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<TenantResponse>> buscarPorNome(@PathVariable String nome) {
        return ResponseEntity.ok(tenantService.buscarPorNome(nome));
    }

    @GetMapping("/documento/{documentoNumero}")
    public ResponseEntity<TenantResponse> buscarPorDocumento(@PathVariable String documentoNumero) {
        return ResponseEntity.ok(tenantService.buscarPorDocumentNumber(documentoNumero));
    }

    @GetMapping("/email")
    public ResponseEntity<TenantResponse> buscarPorEmail(@RequestParam String email) {
        return ResponseEntity.ok(tenantService.buscarPorEmail(email));
    }
    @PutMapping("/documento/{documentoNumero}")
    public ResponseEntity<TenantResponse> update(@PathVariable String documentoNumero, @RequestBody TenantResquest resquest) {
        return ResponseEntity.ok(tenantService.update(documentoNumero, resquest));
    }
}
