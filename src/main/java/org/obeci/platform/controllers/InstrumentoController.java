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
/**
 * Controller REST para persistência e carregamento de instrumentos (slides) e imagens.
 *
 * <p>Instrumento: representação (JSON) associada a uma turma.</p>
 * <p>Imagens: upload e download binário via {@link InstrumentoImage}.</p>
 */
public class InstrumentoController {

    @Autowired
    private InstrumentoService instrumentoService;

    @GetMapping("/turma/{turmaId}")
    /**
     * Busca instrumento da turma. Se não existir, cria um instrumento vazio (compatibilidade).</n+     *
     * <p>Saída: {@link InstrumentoDto} contendo id, turmaId e JSON dos slides.</p>
     */
    public ResponseEntity<InstrumentoDto> getByTurma(@PathVariable Long turmaId) {
        Optional<Instrumento> inst = instrumentoService.getByTurmaId(turmaId);
        if (inst.isPresent()) {
            Instrumento i = inst.get();
            return ResponseEntity.ok(new InstrumentoDto(i.getId(), i.getTurmaId(), i.getSlidesJson()));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/turma/{turmaId}")
    /**
     * Cria ou substitui o JSON de slides do instrumento associado à turma.
     *
     * <p>Entrada: body JSON arbitrário (Jackson {@link JsonNode}).</p>
     * <p>Saída: {@link InstrumentoDto} salvo.</p>
     */
    public ResponseEntity<InstrumentoDto> createOrReplace(@PathVariable Long turmaId, @RequestBody JsonNode slides) throws IOException {
        if (instrumentoService.getByTurmaId(turmaId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Instrumento saved = instrumentoService.saveSlides(turmaId, slides);
        return ResponseEntity.ok(new InstrumentoDto(saved.getId(), saved.getTurmaId(), saved.getSlidesJson()));
    }

    @PutMapping("/turma/{turmaId}")
    /**
        *
        * Regra:
        * - Para acessar um instrumento, ele deve existir previamente.
        * - O instrumento é criado no fluxo de criação de turma.
     *
     * <p>Entrada: body JSON arbitrário (Jackson {@link JsonNode}).</p>
     */
    public ResponseEntity<InstrumentoDto> update(@PathVariable Long turmaId, @RequestBody JsonNode slides) throws IOException {
        if (instrumentoService.getByTurmaId(turmaId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Instrumento saved = instrumentoService.saveSlides(turmaId, slides);
        return ResponseEntity.ok(new InstrumentoDto(saved.getId(), saved.getTurmaId(), saved.getSlidesJson()));
    }

    // Upload de imagem, retorna id e url
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    /**
     * Faz upload de uma imagem e retorna a URL de acesso.
     *
     * <p>Entrada: multipart form-data (campo {@code file}).</p>
     * <p>Saída: String com URL relativa {@code /api/instrumentos/images/{id}}.</p>
     */
    public ResponseEntity<String> uploadImage(@RequestPart("file") MultipartFile file) throws IOException {
        InstrumentoImage img = instrumentoService.saveImage(file);
        String url = "/api/instrumentos/images/" + img.getId();
        return ResponseEntity.ok(url);
    }

    @GetMapping("/images/{id}")
    /**
     * Retorna os bytes de uma imagem previamente enviada.
     *
     * <p>Saída: bytes + Content-Type conforme armazenado.</p>
     */
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
