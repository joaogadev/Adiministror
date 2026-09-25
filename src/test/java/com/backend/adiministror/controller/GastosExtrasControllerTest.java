package com.backend.adiministror.controller;

import com.backend.adiministror.dto.request.GastoExtraRequest;
import com.backend.adiministror.dto.response.GastoExtraResponse;
import com.backend.adiministror.service.GastoExtraService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GastosExtrasControllerTest {

    @Mock
    private GastoExtraService gastoExtraService;

    @InjectMocks
    private GastosExtrasController gastosExtrasController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(gastosExtrasController)
                .build();
    }

    private GastoExtraResponse criarResponse() {
        return new GastoExtraResponse(
                UUID.randomUUID(),
                "Manutenção elétrica",
                "Troca de lâmpadas",
                new BigDecimal("250.00"),
                "2026-09-25",
                "Galeria Central"
        );
    }

    @Test
    void deveCriarGastoExtra() throws Exception {

        UUID galeriaId = UUID.randomUUID();

        GastoExtraResponse response =
                criarResponse();

        when(
                gastoExtraService.createGastoExtra(
                        eq(galeriaId),
                        any(GastoExtraRequest.class)
                )
        ).thenReturn(response);

        String json = """
                {
                    "nome": "Manutenção elétrica",
                    "descricao": "Troca de lâmpadas",
                    "valor": 250.00,
                    "dataGasto": "2026-09-25"
                }
                """;

        /*
         * Valida especialmente:
         * - UUID da galeria vindo da URL;
         * - JSON sendo convertido para GastoExtraRequest;
         * - status correto para criação.
         */
        mockMvc.perform(
                        post(
                                "/gastos-extras/galeria/{galeriaId}",
                                galeriaId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome")
                        .value("Manutenção elétrica"))
                .andExpect(jsonPath("$.valor")
                        .value(250.00))
                .andExpect(jsonPath("$.nomeGaleria")
                        .value("Galeria Central"));

        verify(gastoExtraService)
                .createGastoExtra(
                        eq(galeriaId),
                        any(GastoExtraRequest.class)
                );
    }

    @Test
    void deveBuscarGastoExtraPorId() throws Exception {

        GastoExtraResponse response =
                criarResponse();

        when(
                gastoExtraService.buscar(
                        response.id()
                )
        ).thenReturn(response);

        mockMvc.perform(
                        get(
                                "/gastos-extras/{id}",
                                response.id()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(response.id().toString()))
                .andExpect(jsonPath("$.nome")
                        .value("Manutenção elétrica"));

        verify(gastoExtraService)
                .buscar(response.id());
    }

    @Test
    void deveBuscarGastosExtrasPorNome() throws Exception {

        GastoExtraResponse response =
                criarResponse();

        when(
                gastoExtraService.buscarGastosExtras(
                        "Manutenção"
                )
        ).thenReturn(
                List.of(response)
        );

        // Protege o @RequestParam ?nome=
        mockMvc.perform(
                        get("/gastos-extras/buscar")
                                .param(
                                        "nome",
                                        "Manutenção"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(1))
                .andExpect(jsonPath("$[0].nome")
                        .value("Manutenção elétrica"));

        verify(gastoExtraService)
                .buscarGastosExtras(
                        "Manutenção"
                );
    }

    @Test
    void deveBuscarGastosExtrasPorGaleria() throws Exception {

        UUID galeriaId =
                UUID.randomUUID();

        GastoExtraResponse gasto1 =
                criarResponse();

        GastoExtraResponse gasto2 =
                new GastoExtraResponse(
                        UUID.randomUUID(),
                        "Pintura",
                        "Pintura da fachada",
                        new BigDecimal("900.00"),
                        "2026-09-20",
                        "Galeria Central"
                );

        when(
                gastoExtraService
                        .buscarGastosExtrasPorGaleria(
                                galeriaId
                        )
        ).thenReturn(
                List.of(
                        gasto1,
                        gasto2
                )
        );

        mockMvc.perform(
                        get(
                                "/gastos-extras/galeria/{galeriaId}",
                                galeriaId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[1].nome")
                        .value("Pintura"));

        verify(gastoExtraService)
                .buscarGastosExtrasPorGaleria(
                        galeriaId
                );
    }

    @Test
    void deveBuscarGastosExtrasPorPeriodo() throws Exception {

        UUID galeriaId =
                UUID.randomUUID();

        LocalDate inicio =
                LocalDate.of(2026, 9, 1);

        LocalDate fim =
                LocalDate.of(2026, 9, 30);

        GastoExtraResponse response =
                criarResponse();

        when(
                gastoExtraService
                        .buscarGastosExtrasPorPeriodo(
                                galeriaId,
                                inicio,
                                fim
                        )
        ).thenReturn(
                List.of(response)
        );

        /*
         * Este teste é importante porque valida a conversão:
         *
         * ?inicio=2026-09-01
         *
         * String HTTP -> LocalDate
         */
        mockMvc.perform(
                        get(
                                "/gastos-extras/galeria/{galeriaId}/periodo",
                                galeriaId
                        )
                                .param(
                                        "inicio",
                                        "2026-09-01"
                                )
                                .param(
                                        "fim",
                                        "2026-09-30"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(1))
                .andExpect(jsonPath("$[0].dataGasto")
                        .value("2026-09-25"));

        verify(gastoExtraService)
                .buscarGastosExtrasPorPeriodo(
                        galeriaId,
                        inicio,
                        fim
                );
    }

    @Test
    void deveAtualizarGastoExtra() throws Exception {

        UUID gastoId =
                UUID.randomUUID();

        GastoExtraResponse response =
                new GastoExtraResponse(
                        gastoId,
                        "Manutenção atualizada",
                        "Descrição atualizada",
                        new BigDecimal("300.00"),
                        "2026-09-25",
                        "Galeria Central"
                );

        when(
                gastoExtraService.updateGastoExtra(
                        eq(gastoId),
                        any(GastoExtraRequest.class)
                )
        ).thenReturn(response);

        String json = """
                {
                    "nome": "Manutenção atualizada",
                    "descricao": "Descrição atualizada",
                    "valor": 300.00,
                    "dataGasto": "2026-09-25"
                }
                """;

        mockMvc.perform(
                        put(
                                "/gastos-extras/{id}",
                                gastoId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(gastoId.toString()))
                .andExpect(jsonPath("$.nome")
                        .value("Manutenção atualizada"))
                .andExpect(jsonPath("$.valor")
                        .value(300.00));

        verify(gastoExtraService)
                .updateGastoExtra(
                        eq(gastoId),
                        any(GastoExtraRequest.class)
                );
    }

    @Test
    void deveExcluirGastoExtra() throws Exception {

        UUID gastoId =
                UUID.randomUUID();

        mockMvc.perform(
                        delete(
                                "/gastos-extras/{id}",
                                gastoId
                        )
                )
                .andExpect(status().isNoContent());

        verify(gastoExtraService)
                .deleteGastoExtra(gastoId);
    }
}