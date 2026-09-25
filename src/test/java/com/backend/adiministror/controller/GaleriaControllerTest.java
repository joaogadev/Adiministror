package com.backend.adiministror.controller;

import com.backend.adiministror.dto.request.GaleriaRequest;
import com.backend.adiministror.dto.response.EnderecoResponse;
import com.backend.adiministror.dto.response.GaleriaResponse;
import com.backend.adiministror.dto.response.UsuarioResponse;
import com.backend.adiministror.model.enums.Role;
import com.backend.adiministror.service.GaleriaService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class GaleriaControllerTest {

    @Mock
    private GaleriaService galeriaService;

    @InjectMocks
    private GaleriaController galeriaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(galeriaController)
                .build();
    }

    private GaleriaResponse criarGaleriaResponse() {

        UUID galeriaId = UUID.randomUUID();
        UUID donoId = UUID.randomUUID();

        EnderecoResponse endereco =
                new EnderecoResponse(
                        "49000000",
                        "SE",
                        "Aracaju",
                        "Centro",
                        "Rua A",
                        "100",
                        "Sala 1"
                );

        UsuarioResponse dono =
                new UsuarioResponse(
                        donoId,
                        "Joao",
                        "joao@email.com",
                        "79999999999",
                        Role.DONO
                );

        return new GaleriaResponse(
                galeriaId,
                "Galeria Central",
                "79999999999",
                endereco,
                dono
        );
    }

    @Test
    void deveCriarGaleria() throws Exception {

        GaleriaResponse response =
                criarGaleriaResponse();

        when(galeriaService.create(any(GaleriaRequest.class)))
                .thenReturn(response);

        String json = """
                {
                    "nome": "Galeria Central",
                    "phone": "79999999999",
                    "endereco": {
                        "zipCode": "49000000",
                        "estado": "SE",
                        "cidade": "Aracaju",
                        "bairro": "Centro",
                        "rua": "Rua A",
                        "numero": "100",
                        "complemento": "Sala 1"
                    }
                }
                """;

        mockMvc.perform(
                        post("/galeria")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome")
                        .value("Galeria Central"))
                .andExpect(jsonPath("$.endereco.cidade")
                        .value("Aracaju"))
                .andExpect(jsonPath("$.dono.nome")
                        .value("Joao"));

        verify(galeriaService)
                .create(any(GaleriaRequest.class));
    }

    @Test
    void deveBuscarGaleriaPorId() throws Exception {

        GaleriaResponse response =
                criarGaleriaResponse();

        UUID id = response.id();

        when(galeriaService.buscar(id))
                .thenReturn(response);

        mockMvc.perform(
                        get("/galeria/{id}", id)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(id.toString()))
                .andExpect(jsonPath("$.nome")
                        .value("Galeria Central"));

        verify(galeriaService)
                .buscar(id);
    }

    @Test
    void deveBuscarGaleriaPorNome() throws Exception {

        GaleriaResponse response =
                criarGaleriaResponse();

        when(galeriaService.buscarGalerias("Central"))
                .thenReturn(List.of(response));

        // Aqui validamos principalmente o @RequestParam "nome".
        mockMvc.perform(
                        get("/galeria/buscar")
                                .param("nome", "Central")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome")
                        .value("Galeria Central"));

        verify(galeriaService)
                .buscarGalerias("Central");
    }

    @Test
    void deveBuscarMinhasGalerias() throws Exception {

        GaleriaResponse response =
                criarGaleriaResponse();

        when(galeriaService.buscarMinhasGalerias())
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/galeria/minhas")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome")
                        .value("Galeria Central"));

        verify(galeriaService)
                .buscarMinhasGalerias();
    }

    @Test
    void deveAtualizarGaleria() throws Exception {

        GaleriaResponse response =
                criarGaleriaResponse();

        UUID id = response.id();

        when(
                galeriaService.update(
                        eq(id),
                        any(GaleriaRequest.class)
                )
        ).thenReturn(response);

        String json = """
                {
                    "nome": "Galeria Central",
                    "phone": "79999999999",
                    "endereco": {
                        "zipCode": "49000000",
                        "estado": "SE",
                        "cidade": "Aracaju",
                        "bairro": "Centro",
                        "rua": "Rua A",
                        "numero": "100",
                        "complemento": "Sala 1"
                    }
                }
                """;

        mockMvc.perform(
                        put("/galeria/{id}", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome")
                        .value("Galeria Central"));

        verify(galeriaService)
                .update(
                        eq(id),
                        any(GaleriaRequest.class)
                );
    }

    @Test
    void deveExcluirGaleria() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(
                        delete("/galeria/{id}", id)
                )
                .andExpect(status().isNoContent());

        verify(galeriaService)
                .delete(id);
    }

    @Test
    void deveContarSalasDaGaleria() throws Exception {

        UUID galeriaId = UUID.randomUUID();

        when(galeriaService.contarSalas(galeriaId))
                .thenReturn(5L);

        mockMvc.perform(
                        get(
                                "/galeria/{id}/salas/count",
                                galeriaId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(galeriaService)
                .contarSalas(galeriaId);
    }
}