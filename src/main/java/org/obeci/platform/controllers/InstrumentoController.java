package org.obeci.platform.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.obeci.platform.entities.Instrumento;
import org.obeci.platform.dtos.InstrumentoDto;
import org.obeci.platform.entities.InstrumentoImage;
import org.obeci.platform.services.InstrumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/instrumentos")
public class InstrumentoController {

    @Autowired
    private InstrumentoService instrumentoService;

    @GetMapping("/turma/{turmaId}")
    public ResponseEntity<InstrumentoDto> getByTurma(@PathVariable Long turmaId) {
        Optional<Instrumento> inst = instrumentoService.getByTurmaId(turmaId);
        if (inst.isPresent()) {
            Instrumento i = inst.get();
            return ResponseEntity.ok(new InstrumentoDto(i.getId(), i.getTurmaId(), i.getSlidesJson()));
        }
        // Auto-cria instrumento vazio (2 páginas) para turmas antigas que ainda não possuem
        Instrumento created = instrumentoService.createEmptyForTurma(turmaId);
        return ResponseEntity.ok(new InstrumentoDto(created.getId(), created.getTurmaId(), created.getSlidesJson()));
    }

    @PostMapping("/turma/{turmaId}")
    public ResponseEntity<InstrumentoDto> createOrReplace(@PathVariable Long turmaId, @RequestBody JsonNode slides) throws IOException {
        Instrumento saved = instrumentoService.saveSlides(turmaId, slides);
        return ResponseEntity.ok(new InstrumentoDto(saved.getId(), saved.getTurmaId(), saved.getSlidesJson()));
    }

    @PutMapping("/turma/{turmaId}")
    public ResponseEntity<InstrumentoDto> update(@PathVariable Long turmaId, @RequestBody JsonNode slides) throws IOException {
        Instrumento saved = instrumentoService.saveSlides(turmaId, slides);
        return ResponseEntity.ok(new InstrumentoDto(saved.getId(), saved.getTurmaId(), saved.getSlidesJson()));
    }

    // Upload de imagem, retorna id e url
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImage(@RequestPart("file") MultipartFile file) throws IOException {
        InstrumentoImage img = instrumentoService.saveImage(file);
        String url = "/api/instrumentos/images/" + img.getId();
        return ResponseEntity.ok(url);
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Optional<InstrumentoImage> img = instrumentoService.getImage(id);
        if (img.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(img.get().getContentType()));
        return new ResponseEntity<>(img.get().getData(), headers, HttpStatus.OK);
    }
}
