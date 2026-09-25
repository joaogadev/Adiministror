package com.backend.adiministror.controller;

import com.backend.adiministror.dto.request.ContratoRequest;
import com.backend.adiministror.dto.response.ContratoResponse;
import com.backend.adiministror.service.ContratoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/contratos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DONO')")
public class ContratoController {

    private final ContratoService contratoService;

    @GetMapping("/{id}")
    public ResponseEntity<ContratoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(contratoService.buscar(id));
    }

    @GetMapping("/aluguel/{id}")
    public ResponseEntity<List<ContratoResponse>> buscarPorAluguel(@PathVariable UUID id) {
        return ResponseEntity.ok(contratoService.buscarPorAluguel(id));
    }

    @GetMapping("/aluguel/{alugeulId}/ativo")
    public ResponseEntity<ContratoResponse> buscarContratoAtivo(@PathVariable UUID alugeulId) {
        return ResponseEntity.ok(contratoService.buscarContratoAtivoPorAluguel(alugeulId));
    }

    @GetMapping
    public ResponseEntity<List<ContratoResponse>> buscarTodos() {
        List<ContratoResponse> contratos = contratoService.buscarTodos();
        return ResponseEntity.ok(contratos);
    }

    @GetMapping("/proximo-vencimento")
    public ResponseEntity<List<ContratoResponse>> buscarProximoVencimento() {
        return ResponseEntity.ok(contratoService.buscarProximosDoVencimento());
    }

    @PostMapping("/{id}/renovar")
    public ResponseEntity<ContratoResponse> renovar(
            @PathVariable UUID id,
            @Valid @RequestBody ContratoRequest request
    ) {
        return ResponseEntity.ok(contratoService.renovar(id, request));
    }
}
