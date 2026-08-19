package com.backend.adiministror.service;

import com.backend.adiministror.dto.GaleriaRequest;
import com.backend.adiministror.dto.GaleriaResponse;
import com.backend.adiministror.model.EnderecoModel;
import com.backend.adiministror.model.GaleriaModel;
import com.backend.adiministror.model.UsuarioModel;
import com.backend.adiministror.repository.GaleriaRepository;
import com.backend.adiministror.repository.SalasRepository;
import com.backend.adiministror.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
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
        UUID usuarioAtal = currentUserService.getCurrentUserId();

        UsuarioModel dono = usuarioRepository.findById(usuarioAtal)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        GaleriaModel galeria = galeriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Galeria não encontrada"
                ));

        EnderecoModel endereco = new EnderecoModel(
                request.endereco().zipCode(),
                request.endereco().estado(),
                request.endereco().cidade(),
                request.endereco().bairro(),
                request.endereco().rua(),
                request.endereco().numero(),
                request.endereco().complemento()
        );

        galeria.atualizarDados(
                request.nome().trim(),
                request.phone()
        );

        return GaleriaResponse.from(galeriaRepository.save(galeria));
    }

    public List<GaleriaResponse> buscarGalerias(String nome){
        if (nome == null || nome.trim().isEmpty()){
            throw new RuntimeException("Digite um nome para buscar");
        }

        return galeriaRepository.findByNomeContainingIgnoreCase(nome.trim())
                .stream()
                .map(GaleriaResponse::from)
                .toList();
    }

    public void delete(UUID id){
        GaleriaModel galeria = buscarGaleriaDoUsuario(id);
        galeriaRepository.delete(galeria);
    }

    public GaleriaResponse buscar(UUID id) {

        GaleriaModel galeria = buscarGaleriaDoUsuario(id);
        return GaleriaResponse.from(galeria);
    }

    public long contarSalas(UUID galeriaId) {
        if (!galeriaRepository.existsById(galeriaId)) {
            throw new RuntimeException("Galeria não encontrada");
        }
        return salasRepository.countByGaleriaId(galeriaId);
    }
    public List<GaleriaResponse> buscarMinhasSlas() {
        UUID usuarioAtual = currentUserService.getCurrentUserId();

        return galeriaRepository.findByDono_Id(usuarioAtual)
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

    private GaleriaModel buscarGaleriaDoUsuario(UUID galeriaId) {
        UUID usuarioAtual = currentUserService.getCurrentUserId();

        GaleriaModel galeria = galeriaRepository.findById(galeriaId)
                .orElseThrow(() -> new RuntimeException("Galeria não encontrada"));

        if (!galeria.getDono().getId().equals(usuarioAtual)) {
            throw new RuntimeException("Você não tem permissão para acessar esta galeria");
        }

        return galeria;
    }
}
