package org.obeci.platform.controllers;

import org.obeci.platform.configs.JwtUtil;
import org.obeci.platform.entities.Usuario;
import org.obeci.platform.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
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

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
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
            // Em falha de login, garantir remoção de qualquer cookie de sessão existente
            ResponseCookie clearCookie = ResponseCookie
                .from("token", "")
                .httpOnly(true)
                .secure(false) // Em produção use true (HTTPS)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
            return ResponseEntity.status(401)
                    .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                    .body("Usuário ou senha inválidos");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());

        // Preferência de produção: enviar o JWT em cookie HttpOnly.
        ResponseCookie authCookie = ResponseCookie
            .from("token", jwt)
            .httpOnly(true)
            .secure(false) // Em produção use true (HTTPS)
            .sameSite("Lax")
            .path("/")
            .maxAge(60 * 15) // expira em 15 minutos (exemplo)
            .build();

        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        response.put("username", userDetails.getUsername());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookie.toString())
                .body(response);
    }

    // Endpoint de logout para limpar o cookie HttpOnly.
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie clearCookie = ResponseCookie
            .from("token", "")
            .httpOnly(true)
            .secure(false) // Em produção use true (HTTPS)
            .sameSite("Lax")
            .path("/")
            .maxAge(0)
            .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body("Logout realizado");
    }

    // Retorna informações do usuário autenticado, incluindo roles.
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            ResponseCookie clearCookie = ResponseCookie
                .from("token", "")
                .httpOnly(true)
                .secure(false) // Em produção use true (HTTPS)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

            return ResponseEntity.status(401)
                    .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
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
                    ResponseCookie clearCookie = ResponseCookie
                        .from("token", "")
                        .httpOnly(true)
                        .secure(false) // Em produção use true (HTTPS)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(0)
                        .build();

                    Map<String, Object> err = new HashMap<>();
                    err.put("error", "Usuário não encontrado");
                    return ResponseEntity.status(404)
                            .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                            .body(err);
                });
    }

    // Atualiza dados do próprio usuário autenticado
    @PutMapping("/me")
    public ResponseEntity<?> updateMe(Authentication authentication, @Valid @RequestBody UsuarioSelfUpdateRequest request) {
        if (authentication == null || authentication.getName() == null) {
            ResponseCookie clearCookie = ResponseCookie
                .from("token", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

            return ResponseEntity.status(401)
                    .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
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
                            ResponseCookie authCookie = ResponseCookie
                                .from("token", newJwt)
                                .httpOnly(true)
                                .secure(false) // Em produção use true (HTTPS)
                                .sameSite("Lax")
                                .path("/")
                                .maxAge(60 * 15)
                                .build();
                            return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, authCookie.toString())
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

