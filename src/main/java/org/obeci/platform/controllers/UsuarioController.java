package org.obeci.platform.controllers;

import org.obeci.platform.entities.Usuario;
import org.obeci.platform.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import jakarta.validation.Valid;
import org.obeci.platform.dtos.UsuarioCreateRequest;
import org.obeci.platform.dtos.UsuarioUpdateRequest;
import org.obeci.platform.dtos.ProfessorResponse;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // Lista todos os usuários (ADMIN)
    @GetMapping
    public ResponseEntity<List<Usuario>> findAll() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    // Obtém um usuário por ID (ADMIN)
    @GetMapping("/{id}")
    public ResponseEntity<Optional<Usuario>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    // Cria usuário (ADMIN) — utiliza mesma regra do register, mas sob /api/usuarios
    @PostMapping
    public ResponseEntity<Usuario> create(@Valid @RequestBody UsuarioCreateRequest request) {
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword());
        usuario.setCpf(request.getCpf());
        usuario.setArrayRoles(request.getArrayRoles());
        Usuario created = usuarioService.register(usuario);
        return ResponseEntity.ok(created);
    }

    // Atualiza usuário (ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<Optional<Usuario>> update(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest request) {
        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setEmail(request.getEmail());
        usuario.setCpf(request.getCpf());
        usuario.setArrayRoles(request.getArrayRoles());
        usuario.setPassword(request.getPassword());
        Optional<Usuario> updated = usuarioService.update(id, usuario);
        if (updated.isPresent()) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    // Exclui usuário (ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        boolean deleted = usuarioService.delete(id);
        if (deleted) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }

    // Lista usuários por role com busca opcional (ADMIN)
    @GetMapping("/role/{role}")
    public ResponseEntity<List<Usuario>> findByRole(
            @PathVariable String role,
            @RequestParam(name = "q", required = false) String q) {
        return ResponseEntity.ok(usuarioService.findByRole(role, q));
    }

    // Lista todos os professores (ADMIN) em formato resumido (sem senha)
    @GetMapping("/professores")
    public ResponseEntity<List<ProfessorResponse>> listProfessores() {
        List<Usuario> professores = usuarioService.findByRole("PROFESSOR", null);
        List<ProfessorResponse> result = professores.stream()
                .map(u -> new ProfessorResponse(u.getId(), u.getUsername(), u.getEmail()))
                .toList();
        return ResponseEntity.ok(result);
    }
}
