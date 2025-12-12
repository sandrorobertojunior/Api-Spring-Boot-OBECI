package org.obeci.platform.controllers;

import org.obeci.platform.entities.Publicacao;
import org.obeci.platform.services.PublicacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/publicacoes")
@CrossOrigin(origins = "*")
public class PublicacaoController {

    @Autowired
    private PublicacaoService publicacaoService;

    @GetMapping
    public ResponseEntity<List<Publicacao>> getAllPublicacoes() {
        List<Publicacao> publicacoes = publicacaoService.getAllPublicacoes();
        return ResponseEntity.ok(publicacoes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<Publicacao>> getPublicacaoById(@PathVariable Long id) {
        Optional<Publicacao> publicacao = publicacaoService.getPublicacaoById(id);
        return ResponseEntity.ok(publicacao);
    }

    @PostMapping
    public ResponseEntity<Publicacao> createPublicacao(@RequestBody Publicacao publicacao) {
        Publicacao createdPublicacao = publicacaoService.createPublicacao(publicacao);
        return ResponseEntity.ok(createdPublicacao);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Publicacao> updatePublicacao(@PathVariable Long id, @RequestBody Publicacao publicacaoDetails) {
        Publicacao updatedPublicacao = publicacaoService.updatePublicacao(id, publicacaoDetails);
        if (updatedPublicacao != null) {
            return ResponseEntity.ok(updatedPublicacao);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deletePublicacao(@PathVariable Long id) {
        boolean deleted = publicacaoService.deletePublicacao(id);
        if (deleted) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/turma/{turmaId}")
    public ResponseEntity<List<Publicacao>> getPublicacoesByTurmaId(@PathVariable Long turmaId) {
        List<Publicacao> publicacoes = publicacaoService.getPublicacoesByTurmaId(turmaId);
        return ResponseEntity.ok(publicacoes);
    }

    @GetMapping("/escola/{escolaId}")
    public ResponseEntity<List<Publicacao>> getPublicacoesByEscolaId(@PathVariable Long escolaId) {
        List<Publicacao> publicacoes = publicacaoService.getPublicacoesByEscolaId(escolaId);
        return ResponseEntity.ok(publicacoes);
    }

    @GetMapping("/public/{isPublic}")
    public ResponseEntity<List<Publicacao>> getPublicacoesByIsPublic(@PathVariable Boolean isPublic) {
        List<Publicacao> publicacoes = publicacaoService.getPublicacoesByIsPublic(isPublic);
        return ResponseEntity.ok(publicacoes);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<List<Publicacao>> getPublicacoesByUsername(@PathVariable String username) {
        List<Publicacao> publicacoes = publicacaoService.getPublicacoesByUsername(username);
        return ResponseEntity.ok(publicacoes);
    }
}