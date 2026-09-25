package com.backend.adiministror.service;

import com.backend.adiministror.dto.request.LoginRequest;
import com.backend.adiministror.dto.request.UsuarioRequest;
import com.backend.adiministror.dto.response.LoginResponse;
import com.backend.adiministror.dto.response.UsuarioResponse;
import com.backend.adiministror.exception.BusinessValidationException;
import com.backend.adiministror.exception.ConflictException;
import com.backend.adiministror.exception.ForbidenException;
import com.backend.adiministror.exception.ResourceNotFoundException;
import com.backend.adiministror.model.UsuarioModel;
import com.backend.adiministror.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final CurrentUserService currentUserService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UsuarioResponse create(UsuarioRequest request) {
        String normalizedEmail = normalizedEmail(request.email());
        String normalizedPhone = normalizedPhone(request.phone());

        if (usuarioRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email já cadastrado");
        }

        String senhaHash = passwordEncoder.encode(request.senha());

        UsuarioModel usuario = new UsuarioModel(
                request.nome(),
                normalizedEmail,
                senhaHash,
                normalizedPhone
        );

        UsuarioModel savedUser = usuarioRepository.save(usuario);

        return UsuarioResponse.from(savedUser);
    }

    public LoginResponse login (LoginRequest request) {
        String normalizedEmail = normalizedEmail(request.email());
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    normalizedEmail, request.senha()
            );

            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            UsuarioModel user = (UsuarioModel) authentication.getPrincipal();

            return tokenService.generatedToken(user);
        } catch (Exception e) {
            throw new ForbidenException("Credenciais inválidas");
        }
    }

    public UsuarioResponse update(String email, UsuarioRequest request) {
        String normalizedEmail = normalizedEmail(request.email());
        String normalizedPhone = normalizedPhone(request.phone());

        UsuarioModel usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email não encontrado"));

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
            throw new BusinessValidationException("Email não pode ser vazio");
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
