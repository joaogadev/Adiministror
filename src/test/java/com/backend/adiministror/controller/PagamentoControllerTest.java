package com.backend.adiministror.controller;

import com.backend.adiministror.dto.response.PagamentoResponse;
import com.backend.adiministror.model.enums.PaymentStatus;
import com.backend.adiministror.service.PagamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PagamentoControllerTest {

    @Mock
    private PagamentoService pagamentoService;

    @InjectMocks
    private PagamentoController pagamentoController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        // Simula as requisições HTTP sem iniciar o servidor Spring.
        mockMvc = MockMvcBuilders
                .standaloneSetup(pagamentoController)
                .build();
    }

    private PagamentoResponse criarPagamentoResponse(
            PaymentStatus status
    ) {

        return new PagamentoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 9, 1),
                new BigDecimal("850.00"),
                LocalDate.of(2026, 9, 10),
                status == PaymentStatus.PAGO
                        ? LocalDate.of(2026, 9, 8)
                        : null,
                status
        );
    }

    @Test
    void deveGerarMensalidade() throws Exception {

        UUID aluguelId = UUID.randomUUID();

        PagamentoResponse response =
                criarPagamentoResponse(
                        PaymentStatus.PENDENTE
                );

        when(
                pagamentoService.gerarMensalidade(
                        eq(aluguelId),
                        eq(LocalDate.of(2026, 10, 1))
                )
        ).thenReturn(response);

        String json = """
                {
                    "competencia": "2026-10-01"
                }
                """;

        /*
         * Este teste é importante porque confirma:
         * - aluguelId vindo da URL;
         * - LocalDate sendo convertido do JSON;
         * - POST retornando 201 CREATED.
         */
        mockMvc.perform(
                        post(
                                "/pagamento/aluguel/{aluguelId}",
                                aluguelId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(response.id().toString()))
                .andExpect(jsonPath("$.valor")
                        .value(850.00))
                .andExpect(jsonPath("$.status")
                        .value("PENDENTE"));

        verify(pagamentoService)
                .gerarMensalidade(
                        aluguelId,
                        LocalDate.of(2026, 10, 1)
                );
    }

    @Test
    void deveBuscarPagamentoPorId() throws Exception {

        PagamentoResponse response =
                criarPagamentoResponse(
                        PaymentStatus.PENDENTE
                );

        when(pagamentoService.buscar(response.id()))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/pagamento/{pagamentoId}",
                                response.id()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(response.id().toString()))
                .andExpect(jsonPath("$.competencia")
                        .value("2026-09-01"))
                .andExpect(jsonPath("$.dataVencimento")
                        .value("2026-09-10"));

        verify(pagamentoService)
                .buscar(response.id());
    }

    @Test
    void deveBuscarPagamentosPorAluguel() throws Exception {

        UUID aluguelId = UUID.randomUUID();

        PagamentoResponse pagamento1 =
                criarPagamentoResponse(
                        PaymentStatus.PENDENTE
                );

        PagamentoResponse pagamento2 =
                new PagamentoResponse(
                        UUID.randomUUID(),
                        aluguelId,
                        LocalDate.of(2026, 8, 1),
                        new BigDecimal("850.00"),
                        LocalDate.of(2026, 8, 10),
                        LocalDate.of(2026, 8, 9),
                        PaymentStatus.PAGO
                );

        when(
                pagamentoService.buscarPorAluguel(
                        aluguelId
                )
        ).thenReturn(
                List.of(
                        pagamento1,
                        pagamento2
                )
        );

        mockMvc.perform(
                        get(
                                "/pagamento/aluguel/{aluguelId}",
                                aluguelId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[1].status")
                        .value("PAGO"));

        verify(pagamentoService)
                .buscarPorAluguel(aluguelId);
    }

    @Test
    void deveBuscarPagamentosPendentes() throws Exception {

        PagamentoResponse response =
                criarPagamentoResponse(
                        PaymentStatus.PENDENTE
                );

        when(pagamentoService.buscarPendentes())
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/pagamento/pendentes")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(1))
                .andExpect(jsonPath("$[0].status")
                        .value("PENDENTE"));

        verify(pagamentoService)
                .buscarPendentes();
    }

    @Test
    void deveBuscarPagamentosAtrasados() throws Exception {

        PagamentoResponse response =
                criarPagamentoResponse(
                        PaymentStatus.ATRASADO
                );

        when(pagamentoService.buscarAtrasados())
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/pagamento/atrasados")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status")
                        .value("ATRASADO"));

        verify(pagamentoService)
                .buscarAtrasados();
    }

    @Test
    void deveBuscarPagamentosPagos() throws Exception {

        PagamentoResponse response =
                criarPagamentoResponse(
                        PaymentStatus.PAGO
                );

        when(pagamentoService.buscarPagos())
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/pagamento/pagos")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status")
                        .value("PAGO"))
                .andExpect(jsonPath("$[0].dataPagamento")
                        .value("2026-09-08"));

        verify(pagamentoService)
                .buscarPagos();
    }

    @Test
    void deveBuscarTodosOsPagamentos() throws Exception {

        PagamentoResponse pagamento1 =
                criarPagamentoResponse(
                        PaymentStatus.PENDENTE
                );

        PagamentoResponse pagamento2 =
                criarPagamentoResponse(
                        PaymentStatus.PAGO
                );

        when(pagamentoService.buscarTodos())
                .thenReturn(
                        List.of(
                                pagamento1,
                                pagamento2
                        )
                );

        mockMvc.perform(
                        get("/pagamento")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2));

        verify(pagamentoService)
                .buscarTodos();
    }

    @Test
    void deveRegistrarPagamento() throws Exception {

        UUID pagamentoId = UUID.randomUUID();

        PagamentoResponse response =
                new PagamentoResponse(
                        pagamentoId,
                        UUID.randomUUID(),
                        LocalDate.of(2026, 9, 1),
                        new BigDecimal("850.00"),
                        LocalDate.of(2026, 9, 10),
                        LocalDate.of(2026, 9, 8),
                        PaymentStatus.PAGO
                );

        LocalDate dataPagamento =
                LocalDate.of(2026, 9, 8);

        when(
                pagamentoService.registrarPagamento(
                        pagamentoId,
                        dataPagamento
                )
        ).thenReturn(response);

        String json = """
                {
                    "dataPagamento": "2026-09-08"
                }
                """;

        /*
         * Este teste protege uma das correções que fizemos:
         * o UUID da URL deve ser o ID do PAGAMENTO, não do aluguel.
         */
        mockMvc.perform(
                        patch(
                                "/pagamento/{id}/pagar",
                                pagamentoId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(pagamentoId.toString()))
                .andExpect(jsonPath("$.status")
                        .value("PAGO"))
                .andExpect(jsonPath("$.dataPagamento")
                        .value("2026-09-08"));

        verify(pagamentoService)
                .registrarPagamento(
                        pagamentoId,
                        dataPagamento
                );
    }

    @Test
    void deveAlterarVencimento() throws Exception {

        UUID pagamentoId =
                UUID.randomUUID();

        LocalDate novoVencimento =
                LocalDate.of(2026, 10, 5);

        PagamentoResponse response =
                new PagamentoResponse(
                        pagamentoId,
                        UUID.randomUUID(),
                        LocalDate.of(2026, 9, 1),
                        new BigDecimal("850.00"),
                        novoVencimento,
                        null,
                        PaymentStatus.PENDENTE
                );

        when(
                pagamentoService.alterarVencimento(
                        pagamentoId,
                        novoVencimento
                )
        ).thenReturn(response);

        String json = """
                {
                    "dataVencimento": "2026-10-05"
                }
                """;

        mockMvc.perform(
                        patch(
                                "/pagamento/{id}/vencimento",
                                pagamentoId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataVencimento")
                        .value("2026-10-05"))
                .andExpect(jsonPath("$.status")
                        .value("PENDENTE"));

        verify(pagamentoService)
                .alterarVencimento(
                        pagamentoId,
                        novoVencimento
                );
    }
}