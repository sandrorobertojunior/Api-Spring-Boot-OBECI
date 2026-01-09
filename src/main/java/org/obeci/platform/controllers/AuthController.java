package org.obeci.platform.controllers;

import org.obeci.platform.configs.JwtUtil;
import org.obeci.platform.configs.TokenCookieService;
import org.obeci.platform.entities.Usuario;
import org.obeci.platform.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;

import jakarta.validation.Valid;
import org.obeci.platform.dtos.UsuarioCreateRequest;
import org.obeci.platform.dtos.AuthLoginRequest;
import org.obeci.platform.dtos.UsuarioSelfUpdateRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    private final TokenCookieService tokenCookieService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthController(UsuarioService usuarioService, TokenCookieService tokenCookieService) {
        this.usuarioService = usuarioService;
        this.tokenCookieService = tokenCookieService;
    }

    // Registro de usuário
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UsuarioCreateRequest request) {
        try {
            Usuario novoUsuario = usuarioService.register(request);
            return ResponseEntity.ok(novoUsuario);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthLoginRequest usuario) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usuario.getEmail(), usuario.getPassword())
            );
        } catch (Exception e) {
            // Falha de login: remove cookie de sessão para evitar estado inconsistênte no cliente.
            return ResponseEntity.status(401)
                    .header(HttpHeaders.SET_COOKIE, tokenCookieService.clearAuthCookie().toString())
                    .body("Usuário ou senha inválidos");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());

        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        response.put("username", userDetails.getUsername());

        // Cookie HttpOnly é a forma preferida (principalmente em produção).
        // Manter "token" no corpo serve para compatibilidade com clientes antigos.
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, tokenCookieService.createAuthCookie(jwt).toString())
                .body(response);
    }

    // Endpoint de logout para limpar o cookie HttpOnly.
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Logout: expira o cookie no cliente.
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokenCookieService.clearAuthCookie().toString())
                .body("Logout realizado");
    }

    // Retorna informações do usuário autenticado, incluindo roles.
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            // Não autenticado: devolve 401 e garante limpeza de cookie.
            return ResponseEntity.status(401)
                    .header(HttpHeaders.SET_COOKIE, tokenCookieService.clearAuthCookie().toString())
                    .body("Não autenticado");
        }
        String email = authentication.getName();
        return usuarioService.findByEmail(email)
                .map(u -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("username", u.getUsername());
                    dto.put("email", u.getEmail());
                    dto.put("arrayRoles", u.getArrayRoles());
                    return ResponseEntity.ok(dto);
                })
                .orElseGet(() -> {
                    // Se o token foi aceito mas o usuário não existe mais, limpa cookie por segurança.
                    Map<String, Object> err = new HashMap<>();
                    err.put("error", "Usuário não encontrado");
                    return ResponseEntity.status(404)
                            .header(HttpHeaders.SET_COOKIE, tokenCookieService.clearAuthCookie().toString())
                            .body(err);
                });
    }

    // Atualiza dados do próprio usuário autenticado
    @PutMapping("/me")
    public ResponseEntity<?> updateMe(Authentication authentication, @Valid @RequestBody UsuarioSelfUpdateRequest request) {
        if (authentication == null || authentication.getName() == null) {
            // PUT /auth/me exige autenticação; por segurança, ainda limpamos cookie se não houver auth.
            return ResponseEntity.status(401)
                    .header(HttpHeaders.SET_COOKIE, tokenCookieService.clearAuthCookie().toString())
                    .body("Não autenticado");
        }

        String email = authentication.getName();
        return usuarioService.findByEmail(email)
            .map(u -> {
                Usuario changes = new Usuario();
                changes.setUsername(request.getUsername());
                changes.setEmail(request.getEmail());
                changes.setPassword(request.getPassword());
                changes.setCpf(request.getCpf());

                boolean emailRequestedChange = request.getEmail() != null && !request.getEmail().equalsIgnoreCase(email);

                    return usuarioService.update(u.getId(), changes)
                            .map(updated -> {
                        Map<String, Object> dto = new HashMap<>();
                        dto.put("username", updated.getUsername());
                        dto.put("email", updated.getEmail());
                        dto.put("arrayRoles", updated.getArrayRoles());
                        if (emailRequestedChange) {
                            final String newJwt = jwtUtil.generateToken(updated.getEmail());
                            return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, tokenCookieService.createAuthCookie(newJwt).toString())
                                .body(dto);
                        }
                        return ResponseEntity.ok(dto);
                            })
                            .orElseGet(() -> {
                                Map<String, Object> err = new HashMap<>();
                                err.put("error", "Usuário não encontrado");
                                return ResponseEntity.status(404).body(err);
                            });
            })
                .orElseGet(() -> {
                    Map<String, Object> err = new HashMap<>();
                    err.put("error", "Usuário não encontrado");
                    return ResponseEntity.status(404).body(err);
                });
    }
}

