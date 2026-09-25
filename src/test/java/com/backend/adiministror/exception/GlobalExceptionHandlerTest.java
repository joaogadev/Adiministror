package com.backend.adiministror.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        /*
         * Aqui registramos manualmente nosso GlobalExceptionHandler.
         * Assim testamos exatamente como ele converte Exceptions em HTTP.
         */
        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new ControllerDeTeste()
                )
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();
    }

    @Test
    void deveRetornar404QuandoRecursoNaoExistir()
            throws Exception {

        mockMvc.perform(
                        get("/teste/not-found")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Recurso não encontrado"))
                .andExpect(jsonPath("$.path")
                        .value("/teste/not-found"));
    }

    @Test
    void deveRetornar403QuandoUsuarioNaoTiverPermissao()
            throws Exception {

        mockMvc.perform(
                        get("/teste/forbidden")
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status")
                        .value(403))
                .andExpect(jsonPath("$.error")
                        .value("Forbidden"))
                .andExpect(jsonPath("$.message")
                        .value("Acesso negado"));
    }

    @Test
    void deveRetornar409QuandoExistirConflito()
            throws Exception {

        mockMvc.perform(
                        get("/teste/conflict")
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status")
                        .value(409))
                .andExpect(jsonPath("$.error")
                        .value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Recurso já existe"));
    }

    @Test
    void deveRetornar400ParaRegraDeNegocioInvalida()
            throws Exception {

        mockMvc.perform(
                        get("/teste/business-validation")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Dados inválidos"));
    }

    @Test
    void deveRetornar400ComErrosDosCamposDoDto()
            throws Exception {

        String json = """
                {
                    "nome": ""
                }
                """;

        /*
         * Esse teste é diferente dos anteriores:
         * nenhuma Exception é lançada manualmente.
         *
         * O @Valid gera MethodArgumentNotValidException,
         * e nosso handler precisa transformá-la em 400.
         */
        mockMvc.perform(
                        post("/teste/validacao")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.message")
                        .value("Erro de validação"))
                .andExpect(jsonPath("$.validationErrors.nome")
                        .exists());
    }


    /*
     * Controller existe somente dentro deste teste.
     * Assim não precisamos provocar erros artificiais
     * nos Controllers reais do projeto.
     */
    @RestController
    @RequestMapping("/teste")
    static class ControllerDeTeste {

        @GetMapping("/not-found")
        public void notFound() {

            throw new ResourceNotFoundException(
                    "Recurso não encontrado"
            );
        }

        @GetMapping("/forbidden")
        public void forbidden() {

            throw new ForbidenException(
                    "Acesso negado"
            );
        }

        @GetMapping("/conflict")
        public void conflict() {

            throw new ConflictException(
                    "Recurso já existe"
            );
        }

        @GetMapping("/business-validation")
        public void businessValidation() {

            throw new BusinessValidationException(
                    "Dados inválidos"
            );
        }

        @PostMapping("/validacao")
        public void validar(
                @Valid
                @RequestBody TestRequest request
        ) {
        }
    }


    record TestRequest(

            @NotBlank(
                    message = "Nome é obrigatório"
            )
            String nome

    ) {
    }
}