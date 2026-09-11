package com.backend.adiministror.controller;

import com.backend.adiministror.dto.request.GaleriaRequest;
import com.backend.adiministror.dto.request.SalasRequest;
import com.backend.adiministror.dto.response.SalasResponse;
import com.backend.adiministror.service.SalasService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/salas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DONO')")
public class SalasController {
    private final SalasService salasService;

    @PostMapping("/galeria/{galeriaId}")
    public ResponseEntity<SalasResponse> create(
            @PathVariable UUID galeriaId, @Valid @RequestBody SalasRequest request
            ) {
        SalasResponse salasResponse = salasService.create(galeriaId, request);

        return ResponseEntity.ok().body(salasResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalasResponse> update(@PathVariable UUID id, @Valid @RequestBody SalasRequest request) {
        SalasResponse salasResponse = salasService.update(id, request);

        return ResponseEntity.ok().body(salasResponse);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        salasService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("{id}")
    public ResponseEntity<SalasResponse> buscar(@PathVariable UUID id){
        SalasResponse salas = salasService.buscar(id);

        return ResponseEntity.ok().body(salas);
    }

    @GetMapping("/galeria/{galeriaId}")
    public ResponseEntity<List<SalasResponse>> buscarPorGaleria(@PathVariable UUID galeriaId) {
        List<SalasResponse> salas = salasService.buscarPorGaleria(galeriaId);

        return ResponseEntity.ok().body(salas);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<SalasResponse>> buscarPorNome(@RequestParam String nome) {
        List<SalasResponse> salas = salasService.buscarPorNome(nome);

        return ResponseEntity.ok().body(salas);
    }
}
