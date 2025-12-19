package org.obeci.platform.controllers;

import org.obeci.platform.entities.Escola;
import org.obeci.platform.dtos.EscolaCreateRequest;
import org.obeci.platform.dtos.EscolaUpdateRequest;
import jakarta.validation.Valid;
import org.obeci.platform.services.EscolaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/escolas")
public class EscolaController {

    @Autowired
    private EscolaService escolaService;

    @GetMapping
    public ResponseEntity<List<Escola>> getAllEscolas() {
        List<Escola> escolas = escolaService.getAllEscolas();
        return ResponseEntity.ok(escolas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Escola>> getEscolaById(@PathVariable Long id) {
        Optional<Escola> escola = escolaService.getEscolaById(id);
        return ResponseEntity.ok(escola);
    }

    @PostMapping
    public ResponseEntity<Escola> createEscola(@Valid @RequestBody EscolaCreateRequest request) {
        Escola escola = new Escola();
        escola.setNome(request.getNome());
        escola.setIsActive(request.getIsActive());
        // request.getCidade() presente no DTO, não persistido atualmente
        Escola createdEscola = escolaService.createEscola(escola);
        return ResponseEntity.ok(createdEscola);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Escola> updateEscola(@PathVariable Long id, @Valid @RequestBody EscolaUpdateRequest request) {
        Escola escolaDetails = new Escola();
        escolaDetails.setNome(request.getNome());
        escolaDetails.setIsActive(request.getIsActive());
        Escola updatedEscola = escolaService.updateEscola(id, escolaDetails);
        if (updatedEscola != null) {
            return ResponseEntity.ok(updatedEscola);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteEscola(@PathVariable Long id) {
        boolean deleted = escolaService.deleteEscola(id);
        if (deleted) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/ativo/{isActive}")
    public ResponseEntity<List<Escola>> getActiveEscolas(@PathVariable Boolean isActive) {
        List<Escola> escolas = escolaService.getActiveEscolas(isActive);
        return ResponseEntity.ok(escolas);
    }

    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<Escola>> getEscolasByNomeContaining(@PathVariable String nome) {
        List<Escola> escolas = escolaService.getEscolasByNomeContaining(nome);
        return ResponseEntity.ok(escolas);
    }
}