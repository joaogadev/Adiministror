package com.backend.adiministror.service;

import com.backend.adiministror.model.UsuarioModel;
import com.backend.adiministror.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

public class CurrentUserService {
    private UsuarioRepository userRepository;
    public UUID getCurrentUserId() {
        //pega a ficha do usuario autenticado nessa requisição
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged in");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            return UUID.fromString(jwt.getSubject());
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
    }

    public UsuarioModel getCurrentUser() {
        UUID userid = getCurrentUserId();

        return userRepository.findById(userid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
