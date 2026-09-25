package com.backend.adiministror.service;

import com.backend.adiministror.dto.request.AluguelRequest;
import com.backend.adiministror.model.AluguelModel;
import com.backend.adiministror.model.SalasModel;
import com.backend.adiministror.model.TenantModel;
import com.backend.adiministror.model.enums.StatusAluguel;
import com.backend.adiministror.repository.AlugueisRepository;
import com.backend.adiministror.repository.SalasRepository;
import com.backend.adiministror.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlugueisServiceTest {

    @Mock
    private AlugueisRepository alugueisRepository;

    @Mock
    private SalasRepository salasRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantService tenantService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private PagamentoService pagamentoService;

    @Mock
    private ContratoService contratoService;

    @InjectMocks
    private AlugueisService alugueisService;

    @Test
    void deveLancarErroQuandoNaoExistirSala() {
        UUID salaid = UUID.randomUUID();

        AluguelRequest request = mock(AluguelRequest.class);

        when(salasRepository.findById(salaid)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> alugueisService.create(salaid, request));

        assertEquals("Sala não encontrada", exception.getMessage());

        //verifica se sistema não tentou criar nada depois da validação da sala
        verifyNoInteractions(
                tenantService, pagamentoService, contratoService
        );
    }

    @Test
    void deveLancarErroQuandoSalaJaEstiverAlugada() {
        UUID salaid = UUID.randomUUID();

        AluguelRequest request = mock(AluguelRequest.class);

        SalasModel sala = mock(SalasModel.class);

        when(salasRepository.findById(salaid)).thenReturn(Optional.of(sala));
        when(currentUserService.isAdmin()).thenReturn(true);
        when(alugueisRepository.existsBySala_IdAndStatus(salaid, StatusAluguel.ATIVO)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> alugueisService.create(salaid, request));

        assertEquals("Sala já está alugada", exception.getMessage());

        //verifica se sistema não tentou criar nada depois da validação da sala
        verifyNoInteractions(
                tenantService, pagamentoService, contratoService
        );
    }

    @Test
    void deveEncerrarAluguelEInativarTenantQuandoNaoPossuirOutroAluguel() {
        UUID aluguelid = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        AluguelModel aluguel = mock(AluguelModel.class);
        TenantModel tenant = mock(TenantModel.class);

        when (alugueisRepository.findById(aluguelid)).thenReturn(Optional.of(aluguel));

        when(currentUserService.isAdmin()).thenReturn(true);

        when(aluguel.getStatus()).thenReturn(StatusAluguel.ATIVO);

        when(aluguel.getInquilino()).thenReturn(tenant);

        when(aluguel.getId()).thenReturn(aluguelid);

        when(tenant.getId()).thenReturn(tenantId);

        when(alugueisRepository.existsByInquilino_IdAndStatusAndIdNot(
                tenantId,
                StatusAluguel.ATIVO,
                aluguelid)
        ).thenReturn(false);

        alugueisService.encerrar(aluguelid);

        verify(contratoService).encerrarContratoAtivoPorAluguel(aluguelid);

        verify(aluguel).encerrar();

        verify(alugueisRepository).save(aluguel);

        verify(tenant).desativar();

        verify(tenantRepository).save(tenant);

        verify(alugueisRepository, never()).delete(any());

        verify(tenantRepository, never()).delete(any());
    }

    @Test
    void naoDeveDesativarTenantQuandoPossuirOutroAluguelAtivo() {
        UUID aluguelid = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        AluguelModel aluguel = mock(AluguelModel.class);
        TenantModel tenant = mock(TenantModel.class);

        when(alugueisRepository.findById(aluguelid)).thenReturn(Optional.of(aluguel));

        when(currentUserService.isAdmin()).thenReturn(true);

        when(aluguel.getStatus()).thenReturn(StatusAluguel.ATIVO);

        when(aluguel.getInquilino()).thenReturn(tenant);

        when(aluguel.getId()).thenReturn(aluguelid);

        when(tenant.getId()).thenReturn(tenantId);

        when(alugueisRepository.existsByInquilino_IdAndStatusAndIdNot(
                tenantId,
                StatusAluguel.ATIVO,
                aluguelid)
        ).thenReturn(true);

        alugueisService.encerrar(aluguelid);

        verify(aluguel).encerrar();

        verify(alugueisRepository).save(aluguel);

        verify(contratoService).encerrarContratoAtivoPorAluguel(aluguelid);

        verify(tenant, never()).desativar();

        verify(tenantRepository, never()).save(tenant);
    }

    @Test
    void deveLancarErroAoEncerrarAluguelJaEncerrado() {

        UUID aluguelId = UUID.randomUUID();

        AluguelModel aluguel = mock(AluguelModel.class);

        when(alugueisRepository.findById(aluguelId)
        ).thenReturn(
                Optional.of(aluguel)
        );

        when(currentUserService.isAdmin())
                .thenReturn(true);

        when(aluguel.getStatus())
                .thenReturn(
                        StatusAluguel.ENCERRADO
                );

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> alugueisService.encerrar(
                                aluguelId
                        )
                );

        assertEquals(
                "Aluguel já está encerrado",
                exception.getMessage()
        );

        verifyNoInteractions(
                contratoService
        );

        verify(
                alugueisRepository,
                never()
        ).save(any());

        verify(
                tenantRepository,
                never()
        ).save(any());
    }
}
