package com.backend.adiministror.service;

import com.backend.adiministror.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor //gera construtor para final's
public class UserDetailServices implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    //encontra e devolve o UserModel que implementa o userDetails
    public UserDetails loadUserByUsername(String username) {
        String normalizedEmail = username.trim().toLowerCase();

        return usuarioRepository.findByEmail(normalizedEmail).orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + normalizedEmail));
    }

}
