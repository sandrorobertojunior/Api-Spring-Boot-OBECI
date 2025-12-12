package org.obeci.platform.controllers;

import org.obeci.platform.configs.JwtUtil;
import org.obeci.platform.entities.Usuario;
import org.obeci.platform.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
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
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        try {
            Usuario novoUsuario = usuarioService.register(usuario);
            return ResponseEntity.ok(novoUsuario);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario usuario) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usuario.getEmail(), usuario.getPassword())
            );
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Usuário ou senha inválidos");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());

        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        response.put("username", userDetails.getUsername());

        return ResponseEntity.ok(response);
    }
}

