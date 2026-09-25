package com.backend.adiministror.controller;

import com.backend.adiministror.dto.request.ContratoRequest;
import com.backend.adiministror.dto.response.ContratoResponse;
import com.backend.adiministror.model.enums.StatusContrato;
import com.backend.adiministror.service.ContratoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContratoControllerTest {

    @Mock
    private ContratoService contratoService;

    @InjectMocks
    private ContratoController contratoController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(contratoController)
                .build();
    }

    private ContratoResponse criarContratoResponse(
            StatusContrato status
    ) {

        return new ContratoResponse(
                "Sala 01",
                "Joao Silva",
                "Proprietario",
                LocalDate.of(2026, 9, 25),
                LocalDate.of(2027, 9, 25),
                30,
                LocalDate.of(2027, 8, 26),
                status
        );
    }

    @Test
    void deveBuscarContratoPorId() throws Exception {

        UUID contratoId =
                UUID.randomUUID();

        ContratoResponse response =
                criarContratoResponse(
                        StatusContrato.ATIVO
                );

        when(contratoService.buscar(contratoId))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/contratos/{id}",
                                contratoId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salaNome")
                        .value("Sala 01"))
                .andExpect(jsonPath("$.inquilinoNome")
                        .value("Joao Silva"))
                .andExpect(jsonPath("$.status")
                        .value("ATIVO"));

        verify(contratoService)
                .buscar(contratoId);
    }

    @Test
    void deveBuscarContratosPorAluguel() throws Exception {

        UUID aluguelId =
                UUID.randomUUID();

        ContratoResponse antigo =
                criarContratoResponse(
                        StatusContrato.RENOVADO
                );

        ContratoResponse atual =
                new ContratoResponse(
                        "Sala 01",
                        "Joao Silva",
                        "Proprietario",
                        LocalDate.of(2027, 9, 25),
                        LocalDate.of(2028, 9, 25),
                        30,
                        LocalDate.of(2028, 8, 26),
                        StatusContrato.ATIVO
                );

        when(
                contratoService.buscarPorAluguel(
                        aluguelId
                )
        ).thenReturn(
                List.of(
                        atual,
                        antigo
                )
        );

        /*
         * Esse endpoint representa o histórico do contrato.
         * Por isso esperamos mais de um registro para o mesmo aluguel.
         */
        mockMvc.perform(
                        get(
                                "/contratos/aluguel/{id}",
                                aluguelId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[0].status")
                        .value("ATIVO"))
                .andExpect(jsonPath("$[1].status")
                        .value("RENOVADO"));

        verify(contratoService)
                .buscarPorAluguel(aluguelId);
    }

    @Test
    void deveBuscarContratoAtivoPorAluguel() throws Exception {

        UUID aluguelId =
                UUID.randomUUID();

        ContratoResponse response =
                criarContratoResponse(
                        StatusContrato.ATIVO
                );

        when(
                contratoService
                        .buscarContratoAtivoPorAluguel(
                                aluguelId
                        )
        ).thenReturn(response);

        /*
         * Aqui testamos especialmente o UUID dentro da rota:
         * /aluguel/{aluguelId}/ativo
         */
        mockMvc.perform(
                        get(
                                "/contratos/aluguel/{aluguelId}/ativo",
                                aluguelId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("ATIVO"))
                .andExpect(jsonPath("$.dataFim")
                        .value("2027-09-25"));

        verify(contratoService)
                .buscarContratoAtivoPorAluguel(
                        aluguelId
                );
    }

    @Test
    void deveBuscarTodosOsContratos() throws Exception {

        ContratoResponse contrato1 =
                criarContratoResponse(
                        StatusContrato.ATIVO
                );

        ContratoResponse contrato2 =
                criarContratoResponse(
                        StatusContrato.ENCERRADO
                );

        when(contratoService.buscarTodos())
                .thenReturn(
                        List.of(
                                contrato1,
                                contrato2
                        )
                );

        mockMvc.perform(
                        get("/contratos")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2));

        verify(contratoService)
                .buscarTodos();
    }

    @Test
    void deveBuscarContratosProximosDoVencimento() throws Exception {

        ContratoResponse response =
                criarContratoResponse(
                        StatusContrato.ATIVO
                );

        when(
                contratoService
                        .buscarProximosDoVencimento()
        ).thenReturn(
                List.of(response)
        );

        mockMvc.perform(
                        get(
                                "/contratos/proximo-vencimento"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(1))
                .andExpect(jsonPath("$[0].dataAviso")
                        .value("2027-08-26"))
                .andExpect(jsonPath("$[0].dataFim")
                        .value("2027-09-25"));

        verify(contratoService)
                .buscarProximosDoVencimento();
    }

    @Test
    void deveRenovarContrato() throws Exception {

        UUID contratoId =
                UUID.randomUUID();

        ContratoResponse response =
                new ContratoResponse(
                        "Sala 01",
                        "Joao Silva",
                        "Proprietario",
                        LocalDate.of(2027, 9, 25),
                        LocalDate.of(2028, 9, 25),
                        30,
                        LocalDate.of(2028, 8, 26),
                        StatusContrato.ATIVO
                );

        when(
                contratoService.renovar(
                        eq(contratoId),
                        any(ContratoRequest.class)
                )
        ).thenReturn(response);

        String json = """
                {
                    "dataFim": "2028-09-25",
                    "avisoAntecedenciaDias": 30
                }
                """;

        /*
         * Aqui não testamos novamente a regra de renovação do Service.
         * Testamos se o Controller consegue receber o DTO e chamar
         * contratoService.renovar() com o contrato correto.
         */
        mockMvc.perform(
                        post(
                                "/contratos/{id}/renovar",
                                contratoId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataInicio")
                        .value("2027-09-25"))
                .andExpect(jsonPath("$.dataFim")
                        .value("2028-09-25"))
                .andExpect(jsonPath("$.status")
                        .value("ATIVO"));

        verify(contratoService)
                .renovar(
                        eq(contratoId),
                        any(ContratoRequest.class)
                );
    }
}