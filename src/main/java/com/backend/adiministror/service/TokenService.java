package com.backend.adiministror.service;

import com.backend.adiministror.dto.response.LoginResponse;
import com.backend.adiministror.model.UsuarioModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TokenService {
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long expirationMinutes;

    public TokenService(
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;
    }

    public LoginResponse generatedToken(UsuarioModel usuarioModel) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationMinutes * 60);

        JwtClaimsSet claim = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(usuarioModel.getId().toString())
                .claim("email", usuarioModel.getEmail())
                .claim("role", List.of(usuarioModel.getRole().name()))
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(header, claim))
                .getTokenValue();

        long expiressInSeconsd = expirationMinutes * 60;

        return new LoginResponse(token, "Bearer", expiressInSeconsd);
    }
}
