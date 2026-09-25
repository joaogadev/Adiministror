package com.backend.adiministror.controller;

import com.backend.adiministror.dto.request.SalasRequest;
import com.backend.adiministror.dto.response.SalasResponse;
import com.backend.adiministror.service.SalasService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SalasControllerTest {

    @Mock
    private SalasService salasService;

    @InjectMocks
    private SalasController salasController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(salasController)
                .build();
    }

    private SalasResponse criarSalaResponse() {

        return new SalasResponse(
                UUID.randomUUID(),
                "Sala 01",
                null,
                null
        );
    }

    @Test
    void deveCriarSala() throws Exception {

        UUID galeriaId =
                UUID.randomUUID();

        SalasResponse response =
                criarSalaResponse();

        when(
                salasService.create(
                        eq(galeriaId),
                        any(SalasRequest.class)
                )
        ).thenReturn(response);

        String json = """
                {
                    "nome": "Sala 01"
                }
                """;

        mockMvc.perform(
                        post(
                                "/salas/galeria/{galeriaId}",
                                galeriaId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome")
                        .value("Sala 01"));

        // Também garante que o UUID da URL chegou corretamente ao Service.
        verify(salasService)
                .create(
                        eq(galeriaId),
                        any(SalasRequest.class)
                );
    }

    @Test
    void deveAtualizarSala() throws Exception {

        UUID salaId =
                UUID.randomUUID();

        SalasResponse response =
                new SalasResponse(
                        salaId,
                        "Sala Atualizada",
                        null,
                        null
                );

        when(
                salasService.update(
                        eq(salaId),
                        any(SalasRequest.class)
                )
        ).thenReturn(response);

        String json = """
                {
                    "nome": "Sala Atualizada"
                }
                """;

        mockMvc.perform(
                        put("/salas/{id}", salaId)
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(salaId.toString()))
                .andExpect(jsonPath("$.nome")
                        .value("Sala Atualizada"));

        verify(salasService)
                .update(
                        eq(salaId),
                        any(SalasRequest.class)
                );
    }

    @Test
    void deveExcluirSala() throws Exception {

        UUID salaId =
                UUID.randomUUID();

        mockMvc.perform(
                        delete(
                                "/salas/{id}",
                                salaId
                        )
                )
                .andExpect(status().isNoContent());

        verify(salasService)
                .delete(salaId);
    }

    @Test
    void deveBuscarSalaPorId() throws Exception {

        UUID salaId =
                UUID.randomUUID();

        SalasResponse response =
                new SalasResponse(
                        salaId,
                        "Sala 01",
                        null,
                        null
                );

        when(salasService.buscar(salaId))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/salas/{id}",
                                salaId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(salaId.toString()))
                .andExpect(jsonPath("$.nome")
                        .value("Sala 01"));

        verify(salasService)
                .buscar(salaId);
    }

    @Test
    void deveBuscarSalasPorGaleria() throws Exception {

        UUID galeriaId =
                UUID.randomUUID();

        SalasResponse sala1 =
                new SalasResponse(
                        UUID.randomUUID(),
                        "Sala 01",
                        null,
                        null
                );

        SalasResponse sala2 =
                new SalasResponse(
                        UUID.randomUUID(),
                        "Sala 02",
                        null,
                        null
                );

        when(
                salasService.buscarPorGaleria(
                        galeriaId
                )
        ).thenReturn(
                List.of(sala1, sala2)
        );

        mockMvc.perform(
                        get(
                                "/salas/galeria/{galeriaId}",
                                galeriaId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[0].nome")
                        .value("Sala 01"))
                .andExpect(jsonPath("$[1].nome")
                        .value("Sala 02"));

        verify(salasService)
                .buscarPorGaleria(galeriaId);
    }

    @Test
    void deveBuscarSalaPorNome() throws Exception {

        SalasResponse response =
                criarSalaResponse();

        when(
                salasService.buscarPorNome(
                        "Sala"
                )
        ).thenReturn(
                List.of(response)
        );

        mockMvc.perform(
                        get("/salas/buscar")
                                .param(
                                        "nome",
                                        "Sala"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome")
                        .value("Sala 01"));

        verify(salasService)
                .buscarPorNome("Sala");
    }
}