package com.backend.adiministror.controller;

import com.backend.adiministror.dto.request.LoginRequest;
import com.backend.adiministror.dto.request.UsuarioRequest;
import com.backend.adiministror.dto.response.LoginResponse;
import com.backend.adiministror.dto.response.UsuarioResponse;
import com.backend.adiministror.model.enums.Role;
import com.backend.adiministror.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        // Permite simular requisições HTTP sem precisar iniciar o servidor.
        mockMvc = MockMvcBuilders
                .standaloneSetup(usuarioController)
                .build();
    }

    @Test
    void deveRegistrarUsuario() throws Exception {

        UUID usuarioId = UUID.randomUUID();

        UsuarioResponse response =
                new UsuarioResponse(
                        usuarioId,
                        "Joao Gabriel",
                        "joao@email.com",
                        "79999999999",
                        Role.DONO
                );

        when(usuarioService.create(any(UsuarioRequest.class)))
                .thenReturn(response);

        String json = """
                {
                    "nome": "Joao Gabriel",
                    "email": "joao@email.com",
                    "senha": "Senha@123",
                    "phone": "79999999999"
                }
                """;

        mockMvc.perform(
                        post("/usuario/registro")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        ))
                .andExpect(jsonPath("$.id")
                        .value(usuarioId.toString()))
                .andExpect(jsonPath("$.nome")
                        .value("Joao Gabriel"))
                .andExpect(jsonPath("$.email")
                        .value("joao@email.com"))
                .andExpect(jsonPath("$.phone")
                        .value("79999999999"))
                .andExpect(jsonPath("$.role")
                        .value("DONO"));

        verify(usuarioService)
                .create(any(UsuarioRequest.class));
    }

    @Test
    void deveRealizarLogin() throws Exception {

        LoginResponse response =
                new LoginResponse(
                        "token-jwt-teste",
                        "Bearer",
                        3600
                );

        when(usuarioService.login(any(LoginRequest.class)))
                .thenReturn(response);

        String json = """
                {
                    "email": "joao@email.com",
                    "senha": "Senha@123"
                }
                """;

        mockMvc.perform(
                        post("/usuario/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("token-jwt-teste"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.expiresIn")
                        .value(3600));

        verify(usuarioService)
                .login(any(LoginRequest.class));
    }
}