package com.backend.adiministror.controller;

import com.backend.adiministror.dto.request.LoginRequest;
import com.backend.adiministror.dto.request.UsuarioRequest;
import com.backend.adiministror.dto.response.LoginResponse;
import com.backend.adiministror.dto.response.UsuarioResponse;
import com.backend.adiministror.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
