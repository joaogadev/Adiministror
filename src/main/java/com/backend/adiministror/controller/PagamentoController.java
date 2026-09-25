package com.backend.adiministror.controller;

import com.backend.adiministror.dto.request.AlterarVencimentoRequst;
import com.backend.adiministror.dto.request.GerarPagamentoRequest;
import com.backend.adiministror.dto.request.RegistrarPagamentoRequest;
import com.backend.adiministror.dto.response.PagamentoResponse;
import com.backend.adiministror.service.PagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pagamento")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DONO')")
public class PagamentoController {
    private final PagamentoService pagamentoService;

    @PostMapping("/aluguel/{aluguelId}")
    public ResponseEntity<PagamentoResponse> gerarMensalidade(
            @PathVariable UUID aluguelId,
            @Valid @RequestBody GerarPagamentoRequest request
    ) {
        PagamentoResponse response = pagamentoService.gerarMensalidade(aluguelId, request.competencia());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{pagamentoId}")
    public ResponseEntity<PagamentoResponse> buscar(@PathVariable UUID pagamentoId) {
        PagamentoResponse response = pagamentoService.buscar(pagamentoId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/aluguel/{aluguelId}")
    public ResponseEntity<List<PagamentoResponse>> buscarPorAluguel(@PathVariable UUID aluguelId) {
        return ResponseEntity.ok(pagamentoService.buscarPorAluguel(aluguelId));
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<PagamentoResponse>> buscarPendentes() {
        return ResponseEntity.ok(pagamentoService.buscarPendentes());
    }

    @GetMapping("/atrasados")
    public ResponseEntity<List<PagamentoResponse>> buscarAtrasados() {
        return ResponseEntity.ok(pagamentoService.buscarAtrasados());
    }

    @GetMapping("/pagos")
    public ResponseEntity<List<PagamentoResponse>> buscarPagos() {
        return ResponseEntity.ok(pagamentoService.buscarPagos());
    }

    @GetMapping()
    public ResponseEntity<List<PagamentoResponse>> buscarTodos() {
        return ResponseEntity.ok(pagamentoService.buscarTodos());
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<PagamentoResponse> resgistrarPagamento(
            @PathVariable UUID aluguelId,
            @Valid @RequestBody RegistrarPagamentoRequest request
    ) {
        return ResponseEntity.ok(pagamentoService.registrarPagamento(aluguelId, request.dataPagamento()));
    }

    @PatchMapping("/{id}/vencimento")
    public ResponseEntity<PagamentoResponse> atualizarVencimento(
            @PathVariable UUID id,
            @Valid @RequestBody AlterarVencimentoRequst request
    ) {
        return ResponseEntity.ok(pagamentoService.alterarVencimento(id, request.dataVencimento()));
    }
}
