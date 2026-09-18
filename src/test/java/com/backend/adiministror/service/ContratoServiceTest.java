package com.backend.adiministror.service;

import com.backend.adiministror.dto.request.ContratoRequest;
import com.backend.adiministror.model.*;
import com.backend.adiministror.model.enums.StatusAluguel;
import com.backend.adiministror.model.enums.StatusContrato;
import com.backend.adiministror.repository.AlugueisRepository;
import com.backend.adiministror.repository.ContratoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.TestExecutionResult;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContratoServiceTest {
    @InjectMocks
    private ContratoService contratoService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ContratoRepository contratoRepository;

    @Mock
    private AlugueisRepository alugueisRepository;

    @Test
    void deveCriarContratoInicial() {
        UUID aluguelId = UUID.randomUUID();

        AluguelModel aluguel = mock(AluguelModel.class);

        SalasModel sala = mock(SalasModel.class);

        TenantModel tenant = mock(TenantModel.class);

        GaleriaModel galeria = mock(GaleriaModel.class);

        UsuarioModel usuario = mock(UsuarioModel.class);

        ContratoRequest request = mock(ContratoRequest.class);

        LocalDate inicio = LocalDate.of(2026, 9, 7);
        LocalDate fim = LocalDate.of(2027, 9, 8);

        when(alugueisRepository.findById(aluguelId)).thenReturn(Optional.of(aluguel));
        when(currentUserService.isAdmin()).thenReturn(true);
        when(aluguel.getStatus()).thenReturn(StatusAluguel.ATIVO);

        when(aluguel.getSala()).thenReturn(sala);
        when(aluguel.getInquilino()).thenReturn(tenant);
        when(sala.getGaleria()).thenReturn(galeria);
        when(galeria.getDono()).thenReturn(usuario);
        when(sala.getNome()).thenReturn("Sala 01");
        when(tenant.getNome()).thenReturn("Inquilino 01");
        when(usuario.getNome()).thenReturn("Proprietário 01");
        when(contratoRepository.existsByAluguel_IdAndStatus(aluguelId, StatusContrato.ATIVO)).thenReturn(false);

        when(aluguel.getDataInicio()).thenReturn(inicio);
        when(request.dataFim()).thenReturn(fim);
        when(request.avisoAntecedenciaDias()).thenReturn(30);

        when(contratoRepository.save(
                any(ContratoModel.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        contratoService.criarContratoInicial(aluguelId, request);

        ArgumentCaptor<ContratoModel> captor = ArgumentCaptor.forClass(ContratoModel.class);

        verify(contratoRepository).save(captor.capture());

        ContratoModel contratoModel = captor.getValue();

        assertEquals(inicio, contratoModel.getDataInicio());
        assertEquals(fim, contratoModel.getDataFim());

        assertEquals(30, contratoModel.getAvisoAntecedenciaDias());

        assertEquals(StatusContrato.ATIVO, contratoModel.getStatus());
    }

    @Test
    void naoDeveCriarSegundoContratoAtivo() {
        UUID aluguelId = UUID.randomUUID();

        AluguelModel aluguel = mock(AluguelModel.class);

        ContratoRequest request = mock(ContratoRequest.class);

        when(alugueisRepository.findById(aluguelId)).thenReturn(Optional.of(aluguel));
        when(currentUserService.isAdmin()).thenReturn(true);
        when(aluguel.getStatus()).thenReturn(StatusAluguel.ATIVO);

        when(contratoRepository.existsByAluguel_IdAndStatus(
                aluguelId, StatusContrato.ATIVO)
        ).thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> contratoService.criarContratoInicial(aluguelId, request)
        );

        assertEquals("Já existe um contrato ativo para este aluguel", exception.getMessage());

        verify(contratoRepository, never()).save(
                any()
        );
    }

    @Test
    void naoDeveCriarContratoParaAluguelEncerrado() {
        UUID aluguelId = UUID.randomUUID();

        AluguelModel aluguel = mock(AluguelModel.class);

        ContratoRequest request = mock(ContratoRequest.class);

        when(alugueisRepository.findById(aluguelId)).thenReturn(Optional.of(aluguel));

        when(currentUserService.isAdmin()).thenReturn(true);
        when(aluguel.getStatus()).thenReturn(StatusAluguel.ENCERRADO);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> contratoService.criarContratoInicial(aluguelId, request)
        );

        assertEquals("Não é possível criar contrato para um aluguel encerrado", exception.getMessage());

        verify(contratoRepository, never()).save(
                any()
        );

        verify(contratoRepository, never()).save(any());
    }

    @Test
    void deveRenovarContratoPreservandoContratoAnterior() {
        UUID contratoId = UUID.randomUUID();
        UUID aluguelId = UUID.randomUUID();

        TenantModel tenant = mock(TenantModel.class);
        GaleriaModel galeria = mock(GaleriaModel.class);
        UsuarioModel dono = mock(UsuarioModel.class);
        ContratoModel contrato = mock(ContratoModel.class);
        AluguelModel aluguel = mock(AluguelModel.class);
        SalasModel sala = mock(SalasModel.class);

        ContratoRequest request = mock(ContratoRequest.class);

        LocalDate dataFimAtual = LocalDate.of(2026, 9, 7);
        LocalDate novaDataFim = LocalDate.of(2027, 9, 8);

        when(contratoRepository.findById(contratoId)).thenReturn(Optional.of(contrato));
        when(currentUserService.isAdmin()).thenReturn(true);

        when(aluguel.getSala()).thenReturn(sala);
        when(aluguel.getInquilino()).thenReturn(tenant);
        when(sala.getGaleria()).thenReturn(galeria);
        when(galeria.getDono()).thenReturn(dono);

        when(sala.getNome()).thenReturn("Sala 01");
        when(tenant.getNome()).thenReturn("Inquilino 01");
        when(dono.getNome()).thenReturn("Proprietário 01");
        when(contrato.getStatus()).thenReturn(StatusContrato.ATIVO);
        when(contrato.getAluguel()).thenReturn(aluguel);
        when(contrato.getDataFim()).thenReturn(dataFimAtual);

        when(aluguel.getStatus()).thenReturn(StatusAluguel.ATIVO);

        when(request.dataFim()).thenReturn(novaDataFim);
        when(request.avisoAntecedenciaDias()).thenReturn(30);

        when(contratoRepository.save(any(ContratoModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        contratoService.renovar(contratoId, request);

        verify(contrato).marcarComoRenovado();

        ArgumentCaptor<ContratoModel> captor = ArgumentCaptor.forClass(ContratoModel.class);

        verify(contratoRepository, times(2)).save(captor.capture());

        List<ContratoModel> contratosSalvos =
                captor.getAllValues();

        ContratoModel novoContrato =
                contratosSalvos.get(1);

        assertEquals(dataFimAtual, novoContrato.getDataInicio());
        assertEquals(novaDataFim, novoContrato.getDataFim());
        assertEquals(StatusContrato.ATIVO, novoContrato.getStatus());
    }

    @Test
    void naoDeveRenovarContratoEncerrado() {

        UUID contratoId =
                UUID.randomUUID();

        ContratoModel contrato =
                mock(ContratoModel.class);

        when(contratoRepository.findById(contratoId))
                .thenReturn(
                        Optional.of(contrato)
                );

        when(currentUserService.isAdmin())
                .thenReturn(true);

        when(contrato.getStatus())
                .thenReturn(
                        StatusContrato.ENCERRADO
                );

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> contratoService.renovar(
                                contratoId,
                                mock(ContratoRequest.class)
                        )
                );

        assertEquals(
                "Somente contratos ativos podem ser renovados",
                exception.getMessage()
        );

        verify(
                contratoRepository,
                never()
        ).save(any());
    }


    @Test
    void deveEncerrarContratoAtivoDoAluguel() {

        UUID aluguelId =
                UUID.randomUUID();

        AluguelModel aluguel =
                mock(AluguelModel.class);

        ContratoModel contrato =
                mock(ContratoModel.class);

        when(alugueisRepository.findById(aluguelId))
                .thenReturn(
                        Optional.of(aluguel)
                );

        when(currentUserService.isAdmin())
                .thenReturn(true);

        when(
                contratoRepository
                        .findByAluguel_IdAndStatus(
                                aluguelId,
                                StatusContrato.ATIVO
                        )
        ).thenReturn(
                Optional.of(contrato)
        );

        contratoService
                .encerrarContratoAtivoPorAluguel(
                        aluguelId
                );

        verify(contrato)
                .encerrar();

        verify(contratoRepository)
                .save(contrato);

        verify(
                contratoRepository,
                never()
        ).delete(any());
    }


    @Test
    void naoDeveAceitarDataFimAnteriorAoInicio() {

        UUID aluguelId =
                UUID.randomUUID();

        AluguelModel aluguel =
                mock(AluguelModel.class);

        ContratoRequest request =
                mock(ContratoRequest.class);

        when(alugueisRepository.findById(aluguelId))
                .thenReturn(
                        Optional.of(aluguel)
                );

        when(currentUserService.isAdmin())
                .thenReturn(true);

        when(aluguel.getStatus())
                .thenReturn(StatusAluguel.ATIVO);

        when(
                contratoRepository
                        .existsByAluguel_IdAndStatus(
                                aluguelId,
                                StatusContrato.ATIVO
                        )
        ).thenReturn(false);

        when(aluguel.getDataInicio())
                .thenReturn(
                        LocalDate.of(2026, 9, 10)
                );

        when(request.dataFim())
                .thenReturn(
                        LocalDate.of(2026, 9, 5)
                );

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> contratoService
                                .criarContratoInicial(
                                        aluguelId,
                                        request
                                )
                );

        assertEquals(
                "Data de fim do contrato não pode ser anterior à data de início",
                exception.getMessage()
        );

        verify(
                contratoRepository,
                never()
        ).save(any());
    }
}
