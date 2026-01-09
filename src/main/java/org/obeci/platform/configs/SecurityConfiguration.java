package org.obeci.platform.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private AppCorsProperties corsProperties;

    // Bean para criptografia de senha
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Registro de usuário só pode ser realizado por ADMIN
                .requestMatchers(HttpMethod.POST, "/auth/register").hasRole("ADMIN")
                // Login e logout abertos (logout pode ser feito autenticado, mas manteremos aberto)
                .requestMatchers("/auth/login", "/auth/logout").permitAll()
                // /auth/me: GET pode ser aberto (retorna 401 se não houver token), PUT exige autenticação
                .requestMatchers(HttpMethod.GET, "/auth/me").permitAll()
                .requestMatchers(HttpMethod.PUT, "/auth/me").authenticated()
                // CRUD de usuários somente por ADMIN
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                // Escola: leitura autenticada, escrita somente ADMIN
                .requestMatchers(HttpMethod.GET, "/api/escolas/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/escolas/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/escolas/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/escolas/**").hasRole("ADMIN")
                // Turma: leitura autenticada, escrita somente ADMIN
                .requestMatchers(HttpMethod.GET, "/api/turmas/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/turmas/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/turmas/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/turmas/**").hasRole("ADMIN")
                // Publicação: neste exemplo, exigimos autenticação para todos
                .requestMatchers("/api/publicacoes/**").authenticated()
                // Qualquer outro endpoint exige autenticação
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}