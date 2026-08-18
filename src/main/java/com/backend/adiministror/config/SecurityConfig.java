package com.backend.adiministror.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Bean
    //Transforma chave do properties em chava entendivel pelo java
    //reusmindo um conversão, ja q o o secretKey nn recebe string
    public SecretKey jwtSecretkey() {
        //Aplicação só pra conseguir rodar com chave de teste
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        /*byte[] keyBytes = Base64
                .getDecoder()
                .decode(jwtSecret);*/
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }


    //@Bean
    //esse cara válida todo o processo de login
    /*public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailServices userDetailServices, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailServices);

        //validando as senhas
        provider.setPasswordEncoder(passwordEncoder);

        return  provider;
    }*/

    /*@Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider daoAuthenticationProvider) {
        return new ProviderManager(daoAuthenticationProvider);
    }*/
    @Bean
    //gera token jwt
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        ImmutableSecret<SecurityContext> secret = new ImmutableSecret<>(jwtSecretKey);
        return new NimbusJwtEncoder(secret);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

        //lê as permissoes dos claims com role
        authoritiesConverter.setAuthoritiesClaimName("role");
        //quando ver alguma role trasnforme em ROLE_ROLE_NAME
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

        //transformfa jwt em objeto auhtentication
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    //válida token recebido
    public JwtDecoder jwtDencoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    //criptografa e válida senhas
    public PasswordEncoder passwordEncoder() {
        //codificador com BCrypt como padrão, que mantém no hash a identificação
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        System.out.println("SecurityConfig carregado");
        http
                //não necessário, pois JWT será enviado manualmente no header Authorization
                .csrf(AbstractHttpConfigurer::disable)
                //para não utilizar a página de login padrão do spring
                .formLogin(AbstractHttpConfigurer::disable)
                //para não utilizar http básico
                .httpBasic(AbstractHttpConfigurer::disable)
                //para não criar sessão, pois o JWT é stateless
                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )
                //permite cadastro
                .authorizeHttpRequests(authorize -> authorize
                        //libera dispatch interno de erro.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        //autoriza somente essa rota
                        .requestMatchers("/usuario/**").permitAll()
                        .anyRequest().authenticated()
                )
                //responsavel por informar ao spring que a api é protegida por barear token
                .oauth2ResourceServer(oathh2 ->
                        oathh2.jwt(
                                //Spring valida JWT e também converte o claim "role" em authority
                                jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        ));
        return http.build();
    }
}
