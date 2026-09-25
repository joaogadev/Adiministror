package com.backend.adiministror.service;

import com.backend.adiministror.dto.request.ContratoRequest;
import com.backend.adiministror.dto.response.ContratoResponse;
import com.backend.adiministror.exception.BusinessValidationException;
import com.backend.adiministror.exception.ConflictException;
import com.backend.adiministror.exception.ForbidenException;
import com.backend.adiministror.exception.ResourceNotFoundException;
import com.backend.adiministror.model.AluguelModel;
import com.backend.adiministror.model.ContratoModel;
import com.backend.adiministror.model.enums.StatusAluguel;
import com.backend.adiministror.model.enums.StatusContrato;
import com.backend.adiministror.repository.AlugueisRepository;
import com.backend.adiministror.repository.ContratoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContratoService {
    private final CurrentUserService currentUserService;
    private final ContratoRepository contratoRepository;
    private final AlugueisRepository alugueisRepository;

    @Transactional
    public ContratoResponse criarContratoInicial(UUID aluguelId, ContratoRequest request) {
        AluguelModel aluguel = buscarAluguelAutorizado(aluguelId);

        if (aluguel.getStatus() != StatusAluguel.ATIVO) {
            throw new ConflictException("Não é possível criar contrato para um aluguel encerrado");
        }

        boolean possuicontratoAtivo = contratoRepository.existsByAluguel_IdAndStatus(aluguelId, StatusContrato.ATIVO);

        if (possuicontratoAtivo) {
            throw new ConflictException("Já existe um contrato ativo para este aluguel");
        }

        LocalDate dataInicio = aluguel.getDataInicio();
        validarDatas(dataInicio, request.dataFim());

        int aviso = resolverAvisoAntecedencia(request.avisoAntecedenciaDias());

        ContratoModel contrato = new ContratoModel(
                aluguel,
                dataInicio,
                request.dataFim(),
                aviso
        );

        return ContratoResponse.from(contratoRepository.save(contrato));
    }

    @Transactional
    public ContratoResponse renovar(UUID contratoId, ContratoRequest request) {
        ContratoModel contrato = buscarContratoAutorizado(contratoId);

        if (contrato.getStatus() != StatusContrato.ATIVO) {
            throw new ConflictException("Somente contratos ativos podem ser renovados");
        }

        AluguelModel aluguel = contrato.getAluguel();

        if (aluguel.getStatus() != StatusAluguel.ATIVO) {
            throw new ConflictException("Não é possível renovar contrato de um aluguel encerrado");
        }

        LocalDate novaDataInicio = contrato.getDataFim();
        validarDatas(novaDataInicio, request.dataFim());

        int aviso = resolverAvisoAntecedencia(request.avisoAntecedenciaDias());

        contrato.marcarComoRenovado();

        contratoRepository.save(contrato);

        ContratoModel novoContrato = new ContratoModel(
                aluguel,
                novaDataInicio,
                request.dataFim(),
                aviso
        );

        return ContratoResponse.from(contratoRepository.save(novoContrato));
    }

    public ContratoResponse buscar(UUID contratoId) {
        return ContratoResponse.from(buscarContratoAutorizado(contratoId));
    }

    public ContratoResponse buscarContratoAtivoPorAluguel(UUID aluguelId) {
        buscarAluguelAutorizado(aluguelId);

        ContratoModel contrato = contratoRepository.findByAluguel_IdAndStatus(aluguelId, StatusContrato.ATIVO)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato ativo não encontrado"));

        return ContratoResponse.from(contrato);
    }

    public List<ContratoResponse> buscarPorAluguel(UUID aluguelid) {
        buscarAluguelAutorizado(aluguelid);

        return contratoRepository
                .findByAluguel_IdOrderByDataInicioDesc(aluguelid)
                .stream()
                .map(ContratoResponse::from)
                .toList();
    }

    public List<ContratoResponse> buscarTodos() {
        if (currentUserService.isAdmin()) {
            return contratoRepository.findAll()
                    .stream()
                    .map(ContratoResponse::from)
                    .toList();
        }

        UUID usuarioAtual = currentUserService.getCurrentUserId();

        return contratoRepository.findByAluguel_Sala_Galeria_Dono_Id(usuarioAtual)
                .stream()
                .map(ContratoResponse::from)
                .toList();
    }

    public List<ContratoResponse> buscarProximosDoVencimento() {
        LocalDate hoje = LocalDate.now();

        return buscarTodos()
                .stream()
                .filter(contrato -> contrato.status() == StatusContrato.ATIVO)
                .filter(contrato -> !hoje.isBefore(contrato.dataAviso()))
                .filter(contrato -> !hoje.isAfter(contrato.dataFim()))
                .toList();
    }

    @Transactional
    public void encerrarContratoAtivoPorAluguel(UUID aluguelId) {
        buscarAluguelAutorizado(aluguelId);

        ContratoModel contrato = contratoRepository.findByAluguel_IdAndStatus(aluguelId, StatusContrato.ATIVO)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato ativo não encontrado"));

        contrato.encerrar();

        contratoRepository.save(contrato);
    }

    private void validarDatas(LocalDate dataInicio, LocalDate dataFim) {
        if (dataFim == null) {
            throw new BusinessValidationException("Data de fim do contrato não pode ser nula");
        }

        if (!dataFim.isAfter(dataInicio)) {
            throw new BusinessValidationException("Data de fim do contrato não pode ser anterior à data de início");
        }

    }

    private int resolverAvisoAntecedencia(Integer avisoAntecedencoa) {
        if (avisoAntecedencoa == null) {
            return 30;
        }

        if (avisoAntecedencoa <= 0) {
            throw new BusinessValidationException("Aviso de antecedência não pode ser zero ou negativo");
        }

        return avisoAntecedencoa;
    }
    private ContratoModel buscarContratoAutorizado(UUID contratoId) {
        ContratoModel contrato = contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado"));

        return validarAcesso(contrato);
    }

    private ContratoModel validarAcesso(ContratoModel contrato) {
        if (currentUserService.isAdmin()) {
            return contrato;
        }

        UUID usuarioAtual = currentUserService.getCurrentUserId();

        UUID dono = contrato.getAluguel()
                        .getSala()
                        .getGaleria()
                        .getDono()
                        .getId();

        if (!dono.equals(usuarioAtual)) {
            throw new ForbidenException(
                    "Você não tem permissão para acessar este aluguel"
            );
        }

        return contrato;
    }

    private AluguelModel buscarAluguelAutorizado(UUID aluguelId) {
        AluguelModel aluguel = alugueisRepository.findById(aluguelId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluguel não encontrado"));

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
}
