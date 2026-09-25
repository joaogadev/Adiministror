package com.backend.adiministror.controller;

import com.backend.adiministror.dto.request.TenantResquest;
import com.backend.adiministror.dto.response.TenantResponse;
import com.backend.adiministror.model.enums.DocumentType;
import com.backend.adiministror.service.TenantService;
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
class TenantControllerTest {

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private TenantController tenantController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(tenantController)
                .build();
    }

    private TenantResponse criarTenantResponse() {
        return new TenantResponse(
                UUID.randomUUID(),
                "Joao Silva",
                "79999999999",
                "joao@email.com",
                DocumentType.CPF,
                "12345678900"
        );
    }

    @Test
    void deveBuscarTenantPorId() throws Exception {

        TenantResponse response = criarTenantResponse();

        when(tenantService.buscar(response.id()))
                .thenReturn(response);

        mockMvc.perform(
                        get("/tenants/{id}", response.id())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(response.id().toString()))
                .andExpect(jsonPath("$.nome")
                        .value("Joao Silva"))
                .andExpect(jsonPath("$.email")
                        .value("joao@email.com"));

        verify(tenantService)
                .buscar(response.id());
    }

    @Test
    void deveBuscarTodosOsTenants() throws Exception {

        TenantResponse tenant1 = criarTenantResponse();

        TenantResponse tenant2 = new TenantResponse(
                UUID.randomUUID(),
                "Maria Silva",
                "79888888888",
                "maria@email.com",
                DocumentType.CPF,
                "98765432100"
        );

        when(tenantService.buscarTodos())
                .thenReturn(List.of(tenant1, tenant2));

        mockMvc.perform(
                        get("/tenants")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[0].nome")
                        .value("Joao Silva"))
                .andExpect(jsonPath("$[1].nome")
                        .value("Maria Silva"));

        verify(tenantService)
                .buscarTodos();
    }

    @Test
    void deveBuscarTenantPorNome() throws Exception {

        TenantResponse response = criarTenantResponse();

        when(tenantService.buscarPorNome("Joao"))
                .thenReturn(List.of(response));

        // Confirma que ?nome= foi recebido como @RequestParam.
        mockMvc.perform(
                        get("/tenants/buscar")
                                .param("nome", "Joao")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome")
                        .value("Joao Silva"));

        verify(tenantService)
                .buscarPorNome("Joao");
    }

    @Test
    void deveBuscarTenantPorDocumento() throws Exception {

        String documento = "12345678900";

        TenantResponse response = criarTenantResponse();

        when(tenantService.buscarPorDocumentNumber(documento))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/tenants/documento/{documentoNumero}",
                                documento
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentNumber")
                        .value(documento))
                .andExpect(jsonPath("$.documentType")
                        .value("CPF"));

        verify(tenantService)
                .buscarPorDocumentNumber(documento);
    }

    @Test
    void deveBuscarTenantPorEmail() throws Exception {

        String email = "joao@email.com";

        TenantResponse response = criarTenantResponse();

        when(tenantService.buscarPorEmail(email))
                .thenReturn(response);

        mockMvc.perform(
                        get("/tenants/email")
                                .param("email", email)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email")
                        .value(email));

        verify(tenantService)
                .buscarPorEmail(email);
    }

    @Test
    void deveAtualizarTenantPorDocumento() throws Exception {

        String documento = "12345678900";

        TenantResponse response = new TenantResponse(
                UUID.randomUUID(),
                "Joao Atualizado",
                "79888888888",
                "novo@email.com",
                DocumentType.CPF,
                documento
        );

        when(
                tenantService.update(
                        eq(documento),
                        any(TenantResquest.class)
                )
        ).thenReturn(response);

        String json = """
                {
                    "name": "Joao Atualizado",
                    "phone": "79888888888",
                    "email": "novo@email.com",
                    "documentType": "CPF",
                    "documentNumber": "12345678900"
                }
                """;

        mockMvc.perform(
                        put(
                                "/tenants/documento/{documentoNumero}",
                                documento
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome")
                        .value("Joao Atualizado"))
                .andExpect(jsonPath("$.phone")
                        .value("79888888888"))
                .andExpect(jsonPath("$.email")
                        .value("novo@email.com"));

        verify(tenantService)
                .update(
                        eq(documento),
                        any(TenantResquest.class)
                );
    }
}