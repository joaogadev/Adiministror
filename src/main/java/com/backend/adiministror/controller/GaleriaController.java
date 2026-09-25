package com.backend.adiministror.controller;

import com.backend.adiministror.dto.request.GaleriaRequest;
import com.backend.adiministror.dto.response.GaleriaResponse;
import com.backend.adiministror.service.GaleriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/galeria")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DONO')")
public class GaleriaController {
    private final GaleriaService galeriaService;

    @PostMapping
    public ResponseEntity<GaleriaResponse> create(@Valid @RequestBody GaleriaRequest request) {
        GaleriaResponse galeriaResponse = galeriaService.create(request);

        return ResponseEntity.ok().body(galeriaResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GaleriaResponse> buscar(@PathVariable UUID id) {
        GaleriaResponse galeriaResponse = galeriaService.buscar(id);

        return ResponseEntity.ok().body(galeriaResponse);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<GaleriaResponse>> buscarPorNome(@RequestParam String nome) {
        List<GaleriaResponse> galeriaResponse = galeriaService.buscarGalerias(nome);

        return ResponseEntity.ok().body(galeriaResponse);
    }

    @GetMapping("/minhas")
    public ResponseEntity<List<GaleriaResponse>> buscarMinhasGalerias() {
        List<GaleriaResponse> galeriaResponse = galeriaService.buscarMinhasGalerias();

        return ResponseEntity.ok().body(galeriaResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GaleriaResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody GaleriaRequest request
    ) {
        GaleriaResponse galeriaResponse = galeriaService.update(id, request);

        return ResponseEntity.ok().body(galeriaResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        galeriaService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/salas/count")
    public ResponseEntity<Long> contarSalas(@PathVariable UUID id) {
        return ResponseEntity.ok().body(galeriaService.contarSalas(id));
    }
}