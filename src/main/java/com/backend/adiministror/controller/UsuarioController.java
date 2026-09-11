package com.backend.adiministror.controller;

import com.backend.adiministror.dto.request.LoginRequest;
import com.backend.adiministror.dto.request.UsuarioRequest;
import com.backend.adiministror.dto.response.LoginResponse;
import com.backend.adiministror.dto.response.UsuarioResponse;
import com.backend.adiministror.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuario")
@RequiredArgsConstructor
public class UsuarioController {
    public final UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> createUsuario(@Valid @RequestBody UsuarioRequest usuarioRequest) {
        UsuarioResponse usuario = usuarioService.create(usuarioRequest);

        return ResponseEntity.ok().body(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse login = usuarioService.login(request);

        return ResponseEntity.ok().body(login);
    }

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<ResponseEntity<UsuarioResponse>> buscarTodos() {
        List<UsuarioResponse> usuarios = usuarioService.findAll();

        return usuarios.stream()
                .map(ResponseEntity::ok)
                .toList();
    }
}
