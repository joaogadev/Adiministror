package com.backend.adiministror.service;

import com.backend.adiministror.dto.request.GaleriaRequest;
import com.backend.adiministror.dto.response.GaleriaResponse;
import com.backend.adiministror.model.EnderecoModel;
import com.backend.adiministror.model.GaleriaModel;
import com.backend.adiministror.model.UsuarioModel;
import com.backend.adiministror.repository.GaleriaRepository;
import com.backend.adiministror.repository.SalasRepository;
import com.backend.adiministror.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GaleriaService {
    private final GaleriaRepository galeriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CurrentUserService currentUserService;
    private final SalasRepository salasRepository;

    public GaleriaResponse create(GaleriaRequest request){
        UUID usuarioAtual = currentUserService.getCurrentUserId();

        UsuarioModel dono = usuarioRepository.findById(usuarioAtual)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        EnderecoModel endereco = new EnderecoModel(
                request.endereco().zipCode(),
                request.endereco().estado(),
                request.endereco().cidade(),
                request.endereco().bairro(),
                request.endereco().rua(),
                request.endereco().numero(),
                request.endereco().complemento()
        );

        GaleriaModel galeria = new GaleriaModel(
                request.nome().trim(),
                request.phone(),
                endereco,
                dono
        );

        return GaleriaResponse.from(galeriaRepository.save(galeria));
    }

    public GaleriaResponse update(UUID id, GaleriaRequest request){
         GaleriaModel galeria = buscarGaleriaDoUsuarioAutorizado(id);

        galeria.atualizarDados(
                request.nome().trim(),
                request.phone()
        );

        return GaleriaResponse.from(galeriaRepository.save(galeria));
    }

    public List<GaleriaResponse> buscarGalerias(String nome){
        if (currentUserService.isAdmin()){
            return galeriaRepository
                    .findByNomeContainingIgnoreCase(nome.trim())
                    .stream()
                    .map(GaleriaResponse::from)
                    .toList();
        }

        if (nome == null || nome.trim().isEmpty()){
            throw new RuntimeException("Digite um nome para buscar");
        }

        return galeriaRepository
                .findByNomeContainingIgnoreCaseAndDono_Id(nome.trim(), currentUserService.getCurrentUserId())
                .stream()
                .map(GaleriaResponse::from)
                .toList();
    }

    public void delete(UUID id){
        GaleriaModel galeria = buscarGaleriaDoUsuarioAutorizado(id);
        galeriaRepository.delete(galeria);
    }

    public GaleriaResponse buscar(UUID id) {
        GaleriaModel galeria = buscarGaleriaDoUsuarioAutorizado(id);

        return GaleriaResponse.from(galeria);
    }

    public long contarSalas(UUID galeriaId) {
        buscarGaleriaDoUsuarioAutorizado(galeriaId);
        return salasRepository.countByGaleriaId(galeriaId);
    }
    public List<GaleriaResponse> buscarMinhasGalerias() {
        UUID usuarioAtual = currentUserService.getCurrentUserId();

        return galeriaRepository.findByDono_Id(usuarioAtual)
                .stream()
                .map(GaleriaResponse::from)
                .toList();
    }

    @PreAuthorize("ADMINISTRADOR")
    public List<GaleriaResponse> buscarTodas() {
        return galeriaRepository.findAll()
                .stream()
                .map(GaleriaResponse::from)
                .toList();
    }

    public List<GaleriaResponse> buscarTodasGalerias(String cidade) {
        if (cidade == null || cidade.trim().isEmpty()){
            throw new RuntimeException("Digite um cidade para buscar");
        }

        return galeriaRepository.findByEnderecoCidadeIgnoreCase(cidade)
                .stream()
                .map(GaleriaResponse::from)
                .toList();
    }

    private GaleriaModel buscarGaleriaDoUsuarioAutorizado(UUID galeriaId) {
        UUID usuarioAtual = currentUserService.getCurrentUserId();

        GaleriaModel galeria = galeriaRepository.findById(galeriaId)
                .orElseThrow(() -> new RuntimeException("Galeria não encontrada"));

        if (currentUserService.isAdmin()) {
            return galeria;
        }

        if (!galeria.getDono().getId().equals(usuarioAtual)) {
            throw new RuntimeException("Você não tem permissão para acessar esta galeria");
        }

        return galeria;
    }

    private List<GaleriaResponse> buscarGaleriaPorCidade(UUID galeriaId, String cidade) {
        UUID usuarioAtual = currentUserService.getCurrentUserId();
        GaleriaModel galeria = galeriaRepository.findById(galeriaId)
                .orElseThrow(() -> new RuntimeException("Galeria não encontrada"));

        if (!galeria.getDono().getId().equals(usuarioAtual)) {
            throw new RuntimeException("Você não tem permissão para acessar esta galeria");
        }

        if (cidade == null || cidade.trim().isEmpty()) {
            throw new RuntimeException("Digite uma cidade para buscar");
        }

        if (!galeria.getEndereco().getCidade().equalsIgnoreCase(cidade)) {
            throw new RuntimeException("A galeria não pertence à cidade especificada");
        }

        return galeriaRepository.findByEnderecoCidadeIgnoreCase(cidade.trim())
                .stream()
                .map(GaleriaResponse::from)
                .toList();
    }
}
