package org.obeci.platform.controllers;

import org.obeci.platform.entities.Turma;
import org.obeci.platform.services.TurmaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/turmas")
@CrossOrigin(origins = "*")
public class TurmaController {

    @Autowired
    private TurmaService turmaService;

    @GetMapping
    public ResponseEntity<List<Turma>> getAllTurmas() {
        List<Turma> turmas = turmaService.getAllTurmas();
        return ResponseEntity.ok(turmas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Turma>> getTurmaById(@PathVariable Long id) {
        Optional<Turma> turma = turmaService.getTurmaById(id);
        return ResponseEntity.ok(turma);
    }

    @PostMapping
    public ResponseEntity<Turma> createTurma(@RequestBody Turma turma) {
        Turma createdTurma = turmaService.createTurma(turma);
        return ResponseEntity.ok(createdTurma);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Turma> updateTurma(@PathVariable Long id, @RequestBody Turma turmaDetails) {
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