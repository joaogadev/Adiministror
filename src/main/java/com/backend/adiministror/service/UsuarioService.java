package com.backend.adiministror.service;

import com.backend.adiministror.dto.GaleriaRequest;
import com.backend.adiministror.dto.GaleriaResponse;
import com.backend.adiministror.dto.UsuarioRequest;
import com.backend.adiministror.dto.UsuarioResponse;
import com.backend.adiministror.model.UsuarioModel;
import com.backend.adiministror.repository.GaleriaRepository;
import com.backend.adiministror.repository.SalasRepository;
import com.backend.adiministror.repository.TenantRepository;
import com.backend.adiministror.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final SalasRepository salasRepository;
    private final CurrentUserService currentUserService;
    private final GaleriaRepository galeriaRepository;

    public UsuarioResponse create(UsuarioRequest request) {
        String normalizedEmail = normalizedEmail(request.email());
        String normalizedPhone = normalizedPhone(request.phone());

        if (usuarioRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }

        UsuarioModel usuario = new UsuarioModel(
                request.nome(),
                normalizedEmail,
                request.senha(),
                normalizedPhone
        );

        UsuarioModel savedUser = usuarioRepository.save(usuario);

        return UsuarioResponse.from(savedUser);
    }

    public UsuarioResponse update(String email, UsuarioRequest request) {
        String normalizedEmail = normalizedEmail(request.email());
        String normalizedPhone = normalizedPhone(request.phone());

        UsuarioModel usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email não encontrado"));

        if (!email.equalsIgnoreCase(normalizedEmail)
                && usuarioRepository.existsByEmail(normalizedEmail)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email já cadastrado"
            );
        }

        usuario.atualizrDados(
                request.nome(),
                normalizedEmail,
                normalizedPhone
        );
        usuario.alterarSenha(request.senha());

        UsuarioModel savedUser = usuarioRepository.save(usuario);

        return UsuarioResponse.from(savedUser);

    }

    public List<UsuarioResponse> findAll() {
        return usuarioRepository.findAll()
                .stream()
                .map(user -> UsuarioResponse.from(user))
                .toList();
    }

    public void delete(String email) {
        UsuarioModel currentUser = currentUserService.getCurrentUser();

        usuarioRepository.delete(currentUser);
    }

    private String normalizedEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email não pode ser vazio");
        }

        return email.toLowerCase(Locale.ROOT).trim();
    }

    private String normalizedPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return phone.trim();
    }
}
