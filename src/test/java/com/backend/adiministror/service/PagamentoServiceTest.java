package com.backend.adiministror.service;

import com.backend.adiministror.model.AluguelModel;
import com.backend.adiministror.model.PagamentoModel;
import com.backend.adiministror.model.enums.PaymentStatus;
import com.backend.adiministror.model.enums.StatusAluguel;
import com.backend.adiministror.repository.AlugueisRepository;
import com.backend.adiministror.repository.PagamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PagamentoServiceTest {

    @InjectMocks
    private PagamentoService pagamentoService;

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AlugueisRepository alugueisRepository;

    @Test
    void deveGerarMensalidadeNormalizandoCompetencia() {
        UUID aluguelId = UUID.randomUUID();

        AluguelModel aluguel = mock(AluguelModel.class);

        LocalDate competenciaEsperada = LocalDate.of(2024, 9, 1);
        LocalDate competenciaRecebida = LocalDate.of(2024, 9, 15);

        when(alugueisRepository.findById(aluguelId)).thenReturn(Optional.of(aluguel));
        when(currentUserService.isAdmin()).thenReturn(true);
        when(aluguel.getId()).thenReturn(aluguelId);
        when(aluguel.getStatus()).thenReturn(StatusAluguel.ATIVO);
        when(aluguel.getDataInicio()).thenReturn(LocalDate.of(2024, 3, 1));
        when(aluguel.getDiaVencimentoPadrao()).thenReturn(10);
        when(aluguel.getValorAluguel()).thenReturn(new BigDecimal("800.00"));

        when(pagamentoRepository.save(any(PagamentoModel.class)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );

        pagamentoService.gerarMensalidade(aluguelId, competenciaRecebida);
        ArgumentCaptor<PagamentoModel> captor = ArgumentCaptor.forClass(PagamentoModel.class);
        verify(pagamentoRepository).save(captor.capture());

        PagamentoModel pagamento = captor.getValue();

        assertEquals(competenciaEsperada, pagamento.getCompetencia());

        assertEquals(
                LocalDate.of(2024, 9, 10),
                pagamento.getDataVencimento()
        );

        assertEquals(new BigDecimal("800.00"), pagamento.getValor());
    }

    @Test
    void naoDeveGerarMensalidadeDuplicada() {

        UUID aluguelId = UUID.randomUUID();

        AluguelModel aluguel = mock(AluguelModel.class);

        LocalDate competencia = LocalDate.of(2026, 9, 1);

        when(alugueisRepository.findById(aluguelId)).thenReturn(Optional.of(aluguel));
        when(currentUserService.isAdmin()).thenReturn(true);
        when(aluguel.getId()).thenReturn(aluguelId);
        when(aluguel.getStatus()).thenReturn(StatusAluguel.ATIVO);
        when(aluguel.getDataInicio()).thenReturn(LocalDate.of(2026, 9, 1));

        when(pagamentoRepository.existsByAluguel_IdAndCompetencia(
                                aluguelId,
                                competencia
                        )
        ).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                        () -> pagamentoService.gerarMensalidade(aluguelId, competencia)
        );

        assertEquals("Pagamento já gerado para esta competência", exception.getMessage());

        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void naoDeveGerarMensalidadeParaAluguelEncerrado() {

        UUID aluguelId = UUID.randomUUID();

        AluguelModel aluguel = mock(AluguelModel.class);

        when(alugueisRepository.findById(aluguelId)).thenReturn(Optional.of(aluguel));
        when(currentUserService.isAdmin()).thenReturn(true);
        when(aluguel.getStatus()).thenReturn(StatusAluguel.ENCERRADO);

        RuntimeException exception = assertThrows(RuntimeException.class,
                        () -> pagamentoService.gerarMensalidade(aluguelId,
                                                LocalDate.of(2026, 9, 1)
                                        )
                );

        assertEquals("Aluguel não está ativo", exception.getMessage());

        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void deveGerarPrimeiroPagamentoNoMesmoMesQuandoVencimentoForPosteriorAoInicio() {
        UUID aluguelId = UUID.randomUUID();

        AluguelModel aluguel = mock(AluguelModel.class);

        when(alugueisRepository.findById(aluguelId)).thenReturn(Optional.of(aluguel));
        when(currentUserService.isAdmin()).thenReturn(true);
        when(aluguel.getId()).thenReturn(aluguelId);
        when(aluguel.getStatus()).thenReturn(StatusAluguel.ATIVO);
        when(aluguel.getDataInicio()).thenReturn(LocalDate.of(2026, 9, 5));
        when(aluguel.getDiaVencimentoPadrao())
                .thenReturn(10);

        when(aluguel.getValorAluguel()).thenReturn(new BigDecimal("800.00"));
        when(pagamentoRepository.existsByAluguel_IdAndCompetencia(
                                aluguelId,
                                LocalDate.of(2026, 9, 1)
        )).thenReturn(false);
        when(pagamentoRepository.save(any(PagamentoModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)
                );

        pagamentoService.gerarPrimeiroPagamento(aluguelId);

        ArgumentCaptor<PagamentoModel> captor = ArgumentCaptor.forClass(PagamentoModel.class);

        verify(pagamentoRepository).save(captor.capture());

        assertEquals(LocalDate.of(2026, 9, 1), captor.getValue().getCompetencia());
        assertEquals(LocalDate.of(2026, 9, 10), captor.getValue().getDataVencimento());
    }

    @Test
    void deveGerarPrimeiroPagamentoNoProximoMesQuandoVencimentoJaPassou() {
        UUID aluguelId = UUID.randomUUID();

        AluguelModel aluguel = mock(AluguelModel.class);

        when(alugueisRepository.findById(aluguelId)).thenReturn(Optional.of(aluguel));
        when(currentUserService.isAdmin()).thenReturn(true);
        when(aluguel.getId()).thenReturn(aluguelId);
        when(aluguel.getStatus()).thenReturn(StatusAluguel.ATIVO);
        when(aluguel.getDataInicio()).thenReturn(LocalDate.of(2026, 9, 20));
        when(aluguel.getDiaVencimentoPadrao()).thenReturn(10);
        when(aluguel.getValorAluguel()).thenReturn(new BigDecimal("800.00"));
        when(pagamentoRepository.existsByAluguel_IdAndCompetencia(
                                aluguelId,
                                LocalDate.of(2026, 10, 1))
        ).thenReturn(false);
        when(pagamentoRepository.save(any(PagamentoModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        pagamentoService.gerarPrimeiroPagamento(aluguelId);

        ArgumentCaptor<PagamentoModel> captor = ArgumentCaptor.forClass(PagamentoModel.class);

        verify(pagamentoRepository).save(captor.capture());

        assertEquals(LocalDate.of(2026, 10, 1), captor.getValue().getCompetencia());

        assertEquals(LocalDate.of(2026, 10, 10), captor.getValue().getDataVencimento());
    }

    @Test
    void deveRegistrarPagamento() {
        UUID pagamentoId = UUID.randomUUID();

        PagamentoModel pagamento = mock(PagamentoModel.class);
        AluguelModel aluguel = mock(AluguelModel.class);

        LocalDate dataPagamento = LocalDate.now();

        when(pagamento.getAluguel()).thenReturn(aluguel);
        when(aluguel.getId()).thenReturn(UUID.randomUUID());
        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));
        when(currentUserService.isAdmin()).thenReturn(true);
        when(pagamento.getStatus()).thenReturn(PaymentStatus.PENDENTE);
        when(pagamentoRepository.save(pagamento)).thenReturn(pagamento);

        pagamentoService.registrarPagamento(pagamentoId, dataPagamento);

        verify(pagamento).registrarPagamento(dataPagamento);
        verify(pagamentoRepository).save(pagamento);
    }

    @Test
    void naoDeveRegistrarPagamentoJaPago() {
        UUID pagamentoId = UUID.randomUUID();

        PagamentoModel pagamento = mock(PagamentoModel.class);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(Optional.of(pagamento));
        when(currentUserService.isAdmin()).thenReturn(true);
        when(pagamento.getStatus()).thenReturn(PaymentStatus.PAGO);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> pagamentoService.registrarPagamento(pagamentoId, LocalDate.now())
                );

        assertEquals("Pagamento já registrado", exception.getMessage());

        verify(pagamento, never()).registrarPagamento(any());
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void naoDeveRegistrarPagamentoComDataFutura() {
        UUID pagamentoId = UUID.randomUUID();

        PagamentoModel pagamento = mock(PagamentoModel.class);

        LocalDate amanha = LocalDate.now().plusDays(1);

        when(pagamentoRepository.findById(pagamentoId)).thenReturn(
                Optional.of(pagamento)
        );
        when(currentUserService.isAdmin()).thenReturn(true);
        when(pagamento.getStatus())
                .thenReturn(PaymentStatus.PENDENTE);

        RuntimeException exception = assertThrows(
                        RuntimeException.class,
                        () -> pagamentoService.registrarPagamento(pagamentoId, amanha)
        );

        assertEquals("Data de pagamento não pode ser futura", exception.getMessage());

        verify(pagamento, never()).registrarPagamento(any());
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void deveAlterarDataDeVencimento() {

        UUID pagamentoId = UUID.randomUUID();

        PagamentoModel pagamento = mock(PagamentoModel.class);

        AluguelModel aluguel = mock(AluguelModel.class);

        LocalDate novaData = LocalDate.of(2027, 3, 5);

        when(pagamento.getAluguel()).thenReturn(aluguel);
        when(pagamentoRepository.findById(pagamentoId)).thenReturn(
                Optional.of(pagamento)
        );
        when(currentUserService.isAdmin()).thenReturn(true);
        when(pagamentoRepository.save(pagamento)).thenReturn(pagamento);

        pagamentoService.alterarVencimento(pagamentoId, novaData);

        verify(pagamento).alterarVencimento(novaData);
        verify(pagamentoRepository).save(pagamento);
    }
}
