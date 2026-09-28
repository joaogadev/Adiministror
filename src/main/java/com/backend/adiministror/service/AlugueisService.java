package com.backend.adiministror.service;

import com.backend.adiministror.dto.request.AluguelRequest;
import com.backend.adiministror.dto.request.AluguelUpdateRequest;
import com.backend.adiministror.dto.response.AluguelResponse;
import com.backend.adiministror.exception.ConflictException;
import com.backend.adiministror.exception.ForbidenException;
import com.backend.adiministror.exception.ResourceNotFoundException;
import com.backend.adiministror.model.AluguelModel;
import com.backend.adiministror.model.PagamentoModel;
import com.backend.adiministror.model.SalasModel;
import com.backend.adiministror.model.enums.StatusAluguel;
import com.backend.adiministror.model.TenantModel;
import com.backend.adiministror.repository.AlugueisRepository;
import com.backend.adiministror.repository.PagamentoRepository;
import com.backend.adiministror.repository.SalasRepository;
import com.backend.adiministror.repository.TenantRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlugueisService {
    private final AlugueisRepository alugueisRepository;
    private final SalasRepository salasRepository;
    private final TenantRepository tenantRepository;
    private final TenantService tenantService;
    private final CurrentUserService currentUserService;
    private final PagamentoService pagamentoService;
    private final ContratoService contratoService;
    private final PagamentoRepository pagamentoRepository;

    @Transactional
    public AluguelResponse create(UUID id, AluguelRequest request) {

        SalasModel sala = salasRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sala não encontrada"));

        if (!currentUserService.isAdmin()) {
            UUID usuarioAtual = currentUserService.getCurrentUser().getId();

            if (!sala.getGaleria().getDono().getId().equals(usuarioAtual)) {
                throw new ForbidenException("Você não tem permissão para alugar esta sala");
            }
        }

        boolean possuiAluguelAtivo = alugueisRepository
                .existsBySala_IdAndStatus(id, StatusAluguel.ATIVO);

        if (possuiAluguelAtivo) {
            throw new ConflictException("Sala já está alugada");
        }

        TenantModel tenantSalvo = tenantService.buscarOuCriar(request.inquilino());
        AluguelModel aluguel = new AluguelModel(
                sala,
                tenantSalvo,
                request.dataInicio(),
                request.diaVencimentoPadrao(),
                request.valorAluguel(),
                StatusAluguel.ATIVO
        );

        AluguelModel savedAluguel = alugueisRepository.save(aluguel);

        contratoService.criarContratoInicial(savedAluguel.getId(), request.contrato());

        pagamentoService.gerarPrimeiroPagamento(savedAluguel.getId());

        return AluguelResponse.from(savedAluguel);
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "America/Sao_Paulo") // Executa todo dia 1º do mês à meia-noite no fuso horário de São Paulo
    @Transactional
    public void gerarMensalidadesAutomaticamente() {
        gerarMensalidade();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void verificarMensalidadesAoIniciar() {
        gerarMensalidade();
    }

    @Scheduled(cron = "0 0 1 * * *") // Executa todo dia 1º do mês à meia-noite
    @Transactional
    public void gerarMensalidade() {
        LocalDate competencia = LocalDate.now().withDayOfMonth(1);

        List<AluguelModel> alugueisAtivos = alugueisRepository.findByStatus(StatusAluguel.ATIVO);

        for (AluguelModel aluguel : alugueisAtivos) {
            boolean existe = pagamentoRepository.existsByAluguel_IdAndCompetencia(aluguel.getId(), competencia);

            if (existe) {
                continue;
            }

            LocalDate vencimento = calcularVencimento(aluguel.getDiaVencimentoPadrao(), competencia);

            PagamentoModel pagamento = new PagamentoModel(
                    aluguel,
                    competencia,
                    aluguel.getValorAluguel(),
                    vencimento
            );

            pagamentoRepository.save(pagamento);
        }
    }

    public AluguelResponse buscar(UUID id) {
        AluguelModel aluguel = buscarAlugueisAutorizado(id);

        return AluguelResponse.from(aluguel);
    }

    public AluguelResponse buscarPorSala(UUID salaId) {
        AluguelModel alugel = alugueisRepository
                .findBySala_IdAndStatus(salaId, StatusAluguel.ATIVO)
                .orElseThrow(() -> new RuntimeException("Sala não possui aluguel ativo"));

        return AluguelResponse.from(validarAcesso(alugel));
    }

    public List<AluguelResponse> buscarPorTenant(UUID tenantId) {

        if (currentUserService.isAdmin()) {
            return alugueisRepository
                    .findByInquilino_Id(tenantId)
                    .stream()
                    .map(AluguelResponse::from)
                    .toList();
        }

        UUID usuarioAtual = currentUserService.getCurrentUserId();

        return alugueisRepository.findByInquilino_IdAndSala_Galeria_Dono_Id(tenantId, usuarioAtual)
                .stream()
                .map(AluguelResponse::from)
                .toList();
    }

    public List<AluguelResponse> buscarTodos() {
        if (currentUserService.isAdmin()) {
            return alugueisRepository.findAll()
                    .stream()
                    .map(AluguelResponse::from)
                    .toList();
        }

        UUID usuarioAtual = currentUserService.getCurrentUserId();

        return alugueisRepository.findBySala_Galeria_Dono_Id(usuarioAtual)
                .stream()
                .map(AluguelResponse::from)
                .toList();
    }

    @Transactional
    public AluguelResponse update(UUID id, AluguelUpdateRequest request) {
        AluguelModel aluguel = buscarAlugueisAutorizado(id);

        aluguel.atualizarDados(
            request.diaVencimentoPadrao(),
            request.dataInicio(),
            request.valorAluguel()
        );

        return AluguelResponse.from(alugueisRepository.save(aluguel));
    }

    private AluguelModel buscarAlugueisAutorizado(UUID alugueisId) {
        AluguelModel aluguel = alugueisRepository.findById(alugueisId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluguel não encontrado"));

        return validarAcesso(aluguel);
    }

    private AluguelModel validarAcesso(AluguelModel aluguel) {
        if (currentUserService.isAdmin()) {
            return aluguel;
        }

        UUID usuarioAtual =
                currentUserService.getCurrentUserId();

        UUID dono =
                aluguel.getSala()
                        .getGaleria()
                        .getDono()
                        .getId();

        if (!dono.equals(usuarioAtual)) {
            throw new ForbidenException(
                    "Você não tem permissão para acessar este aluguel"
            );
        }

        return aluguel;
    }

    private LocalDate calcularVencimento(Integer diaVencimento, LocalDate competencia) {
        YearMonth mes = YearMonth.from(competencia);

        int dia = Math.min(diaVencimento, mes.lengthOfMonth());

        return mes.atDay(dia);
    }

    @Transactional
    public void encerrar(UUID id) {
        AluguelModel aluguelModel = buscarAlugueisAutorizado(id);

        if (aluguelModel.getStatus() == StatusAluguel.ENCERRADO) {
            throw new ConflictException("Aluguel já está encerrado");
        }

        TenantModel tenantModel = aluguelModel.getInquilino();

        contratoService.encerrarContratoAtivoPorAluguel(aluguelModel.getId());

        aluguelModel.encerrar();

        alugueisRepository.save(aluguelModel);

        boolean possuiOutrosAlugueisAtivos = alugueisRepository.
                existsByInquilino_IdAndStatusAndIdNot(
                        tenantModel.getId(),
                        StatusAluguel.ATIVO,
                        aluguelModel.getId()
                );

        if (!possuiOutrosAlugueisAtivos) {
            tenantModel.desativar();
            tenantRepository.save(tenantModel);
        }
    }
}
