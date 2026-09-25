package com.backend.adiministror.controller;

import com.backend.adiministror.dto.request.AluguelRequest;
import com.backend.adiministror.dto.request.AluguelUpdateRequest;
import com.backend.adiministror.dto.response.AluguelResponse;
import com.backend.adiministror.model.enums.StatusAluguel;
import com.backend.adiministror.service.AlugueisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AlugueisControllerTest {

    @Mock
    private AlugueisService alugueisService;

    @InjectMocks
    private AlugueisContorller alugueisController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(alugueisController)
                .build();
    }

    private AluguelResponse criarAluguelResponse() {

        return new AluguelResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Sala 01",
                UUID.randomUUID(),
                "Joao Silva",
                "2026-09-25",
                "10",
                StatusAluguel.ATIVO
        );
    }

    @Test
    void deveCriarAluguel() throws Exception {

        UUID salaId = UUID.randomUUID();

        AluguelResponse response =
                criarAluguelResponse();

        when(
                alugueisService.create(
                        eq(salaId),
                        any(AluguelRequest.class)
                )
        ).thenReturn(response);

        String json = """
                {
                    "inquilino": {
                        "name": "Joao Silva",
                        "phone": "79999999999",
                        "email": "joao@email.com",
                        "documentType": "CPF",
                        "documentNumber": "12345678900"
                    },
                    "dataInicio": "2026-09-25",
                    "diaVencimentoPadrao": 10,
                    "valorAluguel": 850.00,
                    "contrato": {
                        "dataFim": "2027-09-25",
                        "avisoAntecedenciaDias": 30
                    }
                }
                """;

        // Este teste também valida a desserialização dos DTOs aninhados.
        mockMvc.perform(
                        post(
                                "/alugueis/sala/{id}",
                                salaId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salaNome")
                        .value("Sala 01"))
                .andExpect(jsonPath("$.tenantNome")
                        .value("Joao Silva"))
                .andExpect(jsonPath("$.dataInicio")
                        .value("2026-09-25"))
                .andExpect(jsonPath("$.status")
                        .value("ATIVO"));

        verify(alugueisService)
                .create(
                        eq(salaId),
                        any(AluguelRequest.class)
                );
    }

    @Test
    void deveBuscarAluguelPorId() throws Exception {

        AluguelResponse response =
                criarAluguelResponse();

        when(alugueisService.buscar(response.id()))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/alugueis/{id}",
                                response.id()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(response.id().toString()))
                .andExpect(jsonPath("$.status")
                        .value("ATIVO"));

        verify(alugueisService)
                .buscar(response.id());
    }

    @Test
    void deveBuscarAluguelPorSala() throws Exception {

        AluguelResponse response =
                criarAluguelResponse();

        UUID salaId = response.salaId();

        when(alugueisService.buscarPorSala(salaId))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/alugueis/sala/{salaId}",
                                salaId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salaId")
                        .value(salaId.toString()))
                .andExpect(jsonPath("$.salaNome")
                        .value("Sala 01"));

        verify(alugueisService)
                .buscarPorSala(salaId);
    }

    @Test
    void deveBuscarAlugueisPorTenant() throws Exception {

        UUID tenantId = UUID.randomUUID();

        AluguelResponse aluguel1 =
                criarAluguelResponse();

        AluguelResponse aluguel2 =
                new AluguelResponse(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Sala 02",
                        tenantId,
                        "Joao Silva",
                        "2025-01-10",
                        "10",
                        StatusAluguel.ENCERRADO
                );

        when(
                alugueisService.buscarPorTenant(
                        tenantId
                )
        ).thenReturn(
                List.of(aluguel1, aluguel2)
        );

        mockMvc.perform(
                        get(
                                "/alugueis/tenant/{tenantId}",
                                tenantId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[1].status")
                        .value("ENCERRADO"));

        verify(alugueisService)
                .buscarPorTenant(tenantId);
    }

    @Test
    void deveBuscarTodosOsAlugueis() throws Exception {

        AluguelResponse aluguel =
                criarAluguelResponse();

        when(alugueisService.buscarTodos())
                .thenReturn(List.of(aluguel));

        mockMvc.perform(
                        get("/alugueis")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(1))
                .andExpect(jsonPath("$[0].salaNome")
                        .value("Sala 01"));

        verify(alugueisService)
                .buscarTodos();
    }

    @Test
    void deveAtualizarAluguel() throws Exception {

        UUID aluguelId = UUID.randomUUID();

        AluguelResponse response =
                new AluguelResponse(
                        aluguelId,
                        UUID.randomUUID(),
                        "Sala 01",
                        UUID.randomUUID(),
                        "Joao Silva",
                        "2026-09-25",
                        "15",
                        StatusAluguel.ATIVO
                );

        when(
                alugueisService.update(
                        eq(aluguelId),
                        any(AluguelUpdateRequest.class)
                )
        ).thenReturn(response);

        String json = """
                {
                    "dataInicio": "2026-09-25",
                    "diaVencimentoPadrao": 15,
                    "valorAluguel": 950.00
                }
                """;

        mockMvc.perform(
                        put(
                                "/alugueis/{id}",
                                aluguelId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(aluguelId.toString()))
                .andExpect(jsonPath("$.dataVencimento")
                        .value("15"));

        verify(alugueisService)
                .update(
                        eq(aluguelId),
                        any(AluguelUpdateRequest.class)
                );
    }

    @Test
    void deveEncerrarAluguel() throws Exception {

        UUID aluguelId = UUID.randomUUID();

        mockMvc.perform(
                        patch(
                                "/alugueis/{id}/encerrar",
                                aluguelId
                        )
                )
                .andExpect(status().isNoContent());

        verify(alugueisService)
                .encerrar(aluguelId);
    }
}