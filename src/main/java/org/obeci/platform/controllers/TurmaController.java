package org.obeci.platform.controllers;

import org.obeci.platform.entities.Turma;
import org.obeci.platform.dtos.TurmaCreateRequest;
import org.obeci.platform.dtos.TurmaUpdateRequest;
import jakarta.validation.Valid;
import org.obeci.platform.services.TurmaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.Collections;
import org.obeci.platform.services.UsuarioService;
import org.obeci.platform.entities.Usuario;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/turmas")
public class TurmaController {

    @Autowired
    private TurmaService turmaService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Turma>> getAllTurmas() {
        List<Turma> turmas = turmaService.getAllTurmas();
        return ResponseEntity.ok(turmas);
    }

    // Retorna turmas visíveis ao usuário atual: ADMIN vê todas; PROFESSOR vê as suas.
    @GetMapping("/mine")
    public ResponseEntity<List<Turma>> getMyTurmas(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body(Collections.<Turma>emptyList());
        }
        String email = authentication.getName();
        return usuarioService.findByEmail(email)
            .map(u -> {
                List<String> roles = u.getArrayRoles() == null ? java.util.Collections.emptyList() : u.getArrayRoles();
                boolean isAdmin = roles.stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r));
                if (isAdmin) {
                    return ResponseEntity.ok(turmaService.getAllTurmas());
                }
                boolean isProfessor = roles.stream().anyMatch(r -> "PROFESSOR".equalsIgnoreCase(r));
                if (isProfessor) {
                    return ResponseEntity.ok(turmaService.getTurmasByProfessorId(u.getId()));
                }
                return ResponseEntity.ok(Collections.<Turma>emptyList());
            })
            .orElseGet(() -> ResponseEntity.status(404).body(Collections.<Turma>emptyList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Turma>> getTurmaById(@PathVariable Long id) {
        Optional<Turma> turma = turmaService.getTurmaById(id);
        return ResponseEntity.ok(turma);
    }

    @PostMapping
    public ResponseEntity<Turma> createTurma(@Valid @RequestBody TurmaCreateRequest request) {
        Turma turma = new Turma();
        turma.setEscolaId(request.getEscolaId());
        turma.setProfessorId(request.getProfessorId());
        turma.setTurno(request.getTurno());
        turma.setNome(request.getNome());
        turma.setIsActive(request.getIsActive());
        Turma createdTurma = turmaService.createTurma(turma);
        return ResponseEntity.ok(createdTurma);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Turma> updateTurma(@PathVariable Long id, @Valid @RequestBody TurmaUpdateRequest request) {
        Turma turmaDetails = new Turma();
        turmaDetails.setEscolaId(request.getEscolaId());
        turmaDetails.setProfessorId(request.getProfessorId());
        turmaDetails.setTurno(request.getTurno());
        turmaDetails.setNome(request.getNome());
        turmaDetails.setIsActive(request.getIsActive());
        Turma updatedTurma = turmaService.updateTurma(id, turmaDetails);
        if (updatedTurma != null) {
            return ResponseEntity.ok(updatedTurma);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteTurma(@PathVariable Long id) {
        boolean deleted = turmaService.deleteTurma(id);
        if (deleted) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/escola/{escolaId}")
    public ResponseEntity<List<Turma>> getTurmasByEscolaId(@PathVariable Long escolaId) {
        List<Turma> turmas = turmaService.getTurmasByEscolaId(escolaId);
        return ResponseEntity.ok(turmas);
    }

    @GetMapping("/professor/{professorId}")
    public ResponseEntity<List<Turma>> getTurmasByProfessorId(@PathVariable Long professorId) {
        List<Turma> turmas = turmaService.getTurmasByProfessorId(professorId);
        return ResponseEntity.ok(turmas);
    }
}