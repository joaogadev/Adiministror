package com.backend.adiministror.controller;

import com.backend.adiministror.dto.request.GastoExtraRequest;
import com.backend.adiministror.dto.response.GastoExtraResponse;
import com.backend.adiministror.service.GastoExtraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/gastos-extras")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DONO')")
public class GastosExtrasController {

    private final GastoExtraService gastoExtraService;

    @PostMapping("/galeria/{galeriaId}")
    public ResponseEntity<GastoExtraResponse> create(
            @PathVariable UUID galeriaId,
            @Valid @RequestBody GastoExtraRequest gastoExtraRequest
    ) {
        GastoExtraResponse gastoExtra = gastoExtraService.createGastoExtra(galeriaId, gastoExtraRequest);
        return ResponseEntity.ok().body(gastoExtra);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GastoExtraResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(gastoExtraService.buscar(id));
    }

    @GetMapping("/buscar-nome")
    public ResponseEntity<List<GastoExtraResponse>> buscarPorNome(@RequestParam String nome) {
        return ResponseEntity.ok(gastoExtraService.buscarGastosExtras(nome));
    }

    @GetMapping("/galeria/{galeriaId}")
    public ResponseEntity<List<GastoExtraResponse>> buscarPorGaleria(@PathVariable UUID galeriaId) {
        return ResponseEntity.ok(gastoExtraService.buscarGastosExtrasPorGaleria(galeriaId));
    }

    @GetMapping("/galeria/{galeriaId}/periodo")
    public ResponseEntity<List<GastoExtraResponse>> buscarPorPerido(
            @PathVariable UUID galeriaId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate inicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fim

    ) {
        return ResponseEntity.ok(gastoExtraService.buscarGastosExtrasPorPeriodo(galeriaId, inicio, fim));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GastoExtraResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody GastoExtraRequest gastoExtraRequest
    ) {
        return ResponseEntity.ok(gastoExtraService.updateGastoExtra(id, gastoExtraRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        gastoExtraService.deleteGastoExtra(id);
        return ResponseEntity.noContent().build();
    }
}
