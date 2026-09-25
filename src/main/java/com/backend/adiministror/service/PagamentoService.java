package com.backend.adiministror.service;

import com.backend.adiministror.dto.response.PagamentoResponse;
import com.backend.adiministror.exception.BusinessValidationException;
import com.backend.adiministror.exception.ConflictException;
import com.backend.adiministror.exception.ForbidenException;
import com.backend.adiministror.exception.ResourceNotFoundException;
import com.backend.adiministror.model.AluguelModel;
import com.backend.adiministror.model.PagamentoModel;
import com.backend.adiministror.model.enums.PaymentStatus;
import com.backend.adiministror.model.enums.StatusAluguel;
import com.backend.adiministror.repository.AlugueisRepository;
import com.backend.adiministror.repository.PagamentoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagamentoService {
    private final PagamentoRepository pagamentoRepository;
    private final CurrentUserService currentUserService;
    private final AlugueisRepository alugueisRepository;

    @Transactional
    public PagamentoResponse gerarMensalidade(UUID id, LocalDate competencia) {
        AluguelModel aluguel = buscarAluguelAutorizado(id);

        if (aluguel.getStatus() != StatusAluguel.ATIVO) {
            throw new ConflictException("Aluguel não está ativo");
        }

        LocalDate competenciaNormalized = competencia.withDayOfMonth(1);

        LocalDate competenciaInicioAluguel = aluguel.getDataInicio().withDayOfMonth(1);

        if (competenciaNormalized.isBefore(competenciaInicioAluguel)) {
            throw new BusinessValidationException("Não é possivel gerar pagamento anterior ao inicio do aluguel");
        }

        boolean pagamentoExistente = pagamentoRepository
                .existsByAluguel_IdAndCompetencia(aluguel.getId(), competenciaNormalized);

        if (pagamentoExistente) {
            throw new ConflictException("Pagamento já gerado para esta competência");
        }

        LocalDate dataVencimento = calcularDataVencimento(aluguel.getDiaVencimentoPadrao(), competenciaNormalized);

        PagamentoModel pagamento = new PagamentoModel(
                aluguel,
                competenciaNormalized,
                aluguel.getValorAluguel(),
                dataVencimento
        );

        return PagamentoResponse.from(pagamentoRepository.save(pagamento));
    }

    @Transactional
    public PagamentoResponse alterarVencimento(UUID pagamentoId, LocalDate novaDataVencimento) {
        PagamentoModel pagamento = buscarPagamentoAutorizado(pagamentoId);

        pagamento.alterarVencimento(novaDataVencimento);

        return PagamentoResponse.from(pagamentoRepository.save(pagamento));
    }

    @Transactional
    public PagamentoResponse registrarPagamento(UUID pagamentoId, LocalDate dataPagamento) {
        PagamentoModel pagamento = buscarPagamentoAutorizado(pagamentoId);

        if (pagamento.getStatus() == PaymentStatus.PAGO) {
            throw new ConflictException("Pagamento já registrado");
        }

        if (dataPagamento.isAfter(LocalDate.now())) {
            throw new BusinessValidationException("Data de pagamento não pode ser futura");
        }

        pagamento.registrarPagamento(dataPagamento);

        return PagamentoResponse.from(pagamentoRepository.save(pagamento));
    }

    public PagamentoResponse buscar(UUID pagamentoId) {
        return PagamentoResponse.from(buscarPagamentoAutorizado(pagamentoId));
    }

    public List<PagamentoResponse> buscarPorAluguel(UUID aluguelId) {
        AluguelModel aluguel = buscarAluguelAutorizado(aluguelId);

        return pagamentoRepository
                .findByAluguel_IdOrderByCompetenciaDesc(aluguel.getId())
                .stream()
                .map(PagamentoResponse::from)
                .toList();
    }

    public List<PagamentoResponse> buscarTodos() {
        if (currentUserService.isAdmin()) {
            return pagamentoRepository.findAll()
                    .stream()
                    .map(PagamentoResponse::from)
                    .toList();
        }

        UUID usuarioAtual = currentUserService.getCurrentUserId();

        return pagamentoRepository.findByAluguel_Sala_Galeria_Dono_Id(usuarioAtual)
                .stream()
                .map(PagamentoResponse::from)
                .toList();
    }

    public List<PagamentoResponse> buscarAtrasados() {
        return buscarTodos().stream()
                .filter(pagamento -> pagamento.status() == PaymentStatus.ATRASADO)
                .toList();
    }

    public List<PagamentoResponse> buscarPendentes() {
        return buscarTodos().stream()
                .filter(pagamento -> pagamento.status() == PaymentStatus.PENDENTE)
                .toList();
    }

    public List<PagamentoResponse> buscarPagos() {
        return buscarTodos().stream()
                .filter(pagamento -> pagamento.status() == PaymentStatus.PAGO)
                .toList();
    }



    private LocalDate calcularDataVencimento(Integer diaVencimentoPadrao, LocalDate competenciaNormalized) {
        YearMonth mes = YearMonth.from(competenciaNormalized);

        int ultimoDiaDoMes = mes.lengthOfMonth();

        int dia = Math.min(diaVencimentoPadrao, ultimoDiaDoMes);

        return mes.atDay(dia);
    }

    private PagamentoModel buscarPagamentoAutorizado(UUID pagamentoId) {
        PagamentoModel pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado"));

        if (currentUserService.isAdmin()) {
            return pagamento;
        }

        UUID usuarioAtual =
                currentUserService.getCurrentUserId();

        UUID dono =
                pagamento.getAluguel().getSala()
                        .getGaleria()
                        .getDono()
                        .getId();

        if (!dono.equals(usuarioAtual)) {
            throw new ForbidenException(
                    "Você não tem permissão para acessar este aluguel"
            );
        }

        return pagamento;
    }

    private AluguelModel buscarAluguelAutorizado(UUID aluguelId) {
        AluguelModel aluguel =
                alugueisRepository
                        .findById(aluguelId)
                        .orElseThrow(() -> new ResourceNotFoundException("Aluguel não encontrado"));

        if (currentUserService.isAdmin()) {
            return aluguel;
        }

        UUID usuarioAtual =
                currentUserService.getCurrentUserId();

        UUID dono = aluguel.getSala().getGaleria().getDono().getId();

        if (!dono.equals(usuarioAtual)) {
            throw new ForbidenException(
                    "Você não tem permissão para acessar este aluguel"
            );
        }

        return aluguel;
    }

    @Transactional
    public PagamentoResponse gerarPrimeiroPagamento(UUID id) {
        AluguelModel aluguel = buscarAluguelAutorizado(id);

        LocalDate competencia = aluguel.getDataInicio().withDayOfMonth(1);

        LocalDate dataVencimento = calcularDataVencimento(aluguel.getDiaVencimentoPadrao(), competencia);

        if (dataVencimento.isBefore(aluguel.getDataInicio())) {
            competencia = competencia.plusMonths(1);
        }

        return gerarMensalidade(id, competencia);
    }
}
