package org.obeci.platform.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.obeci.platform.entities.Instrumento;
import org.obeci.platform.entities.InstrumentoImage;
import org.obeci.platform.repositories.InstrumentoImageRepository;
import org.obeci.platform.repositories.InstrumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class InstrumentoService {

    @Autowired
    private InstrumentoRepository instrumentoRepository;

    @Autowired
    private InstrumentoImageRepository imageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Optional<Instrumento> getByTurmaId(Long turmaId) {
        return instrumentoRepository.findByTurmaId(turmaId);
    }

    @Transactional
    public Instrumento createEmptyForTurma(Long turmaId) {
        if (instrumentoRepository.existsByTurmaId(turmaId)) {
            return instrumentoRepository.findByTurmaId(turmaId).get();
        }
        Instrumento instrumento = new Instrumento();
        instrumento.setTurmaId(turmaId);
        instrumento.setSlidesJson(defaultSlidesJson());
        return instrumentoRepository.save(instrumento);
    }

    @Transactional
    public Instrumento saveSlides(Long turmaId, JsonNode slidesNode) throws IOException {
        Instrumento instrumento = instrumentoRepository.findByTurmaId(turmaId)
                .orElseGet(() -> createEmptyForTurma(turmaId));
        String json = objectMapper.writeValueAsString(slidesNode);
        instrumento.setSlidesJson(json);
        return instrumentoRepository.save(instrumento);
    }

    public InstrumentoImage saveImage(MultipartFile file) throws IOException {
        InstrumentoImage img = new InstrumentoImage();
        img.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
        img.setOriginalName(file.getOriginalFilename());
        img.setData(file.getBytes());
        return imageRepository.save(img);
    }

    public Optional<InstrumentoImage> getImage(Long id) {
        return imageRepository.findById(id);
    }

    private String defaultSlidesJson() {
        // Cria dois slides iniciais vazios compatíveis com o editor
        ArrayNode arr = objectMapper.createArrayNode();

        ObjectNode slide1 = objectMapper.createObjectNode();
        slide1.put("id", 1);
        slide1.put("content", "");
        ObjectNode styles1 = objectMapper.createObjectNode();
        styles1.put("fontSize", "24px");
        styles1.put("fontWeight", "normal");
        styles1.put("fontStyle", "normal");
        styles1.put("textDecoration", "none");
        styles1.put("fontFamily", "Nunito");
        slide1.set("styles", styles1);
        slide1.set("textBoxes", objectMapper.createArrayNode());
        slide1.set("images", objectMapper.createArrayNode());
        slide1.putNull("instrument");
        slide1.set("tags", objectMapper.createArrayNode());
        arr.add(slide1);

        ObjectNode slide2 = objectMapper.createObjectNode();
        slide2.put("id", 2);
        slide2.put("content", "");
        ObjectNode styles2 = objectMapper.createObjectNode();
        styles2.put("fontSize", "24px");
        styles2.put("fontWeight", "normal");
        styles2.put("fontStyle", "normal");
        styles2.put("textDecoration", "none");
        styles2.put("fontFamily", "Nunito");
        slide2.set("styles", styles2);
        slide2.set("textBoxes", objectMapper.createArrayNode());
        slide2.set("images", objectMapper.createArrayNode());
        slide2.putNull("instrument");
        slide2.set("tags", objectMapper.createArrayNode());
        arr.add(slide2);

        return arr.toString();
    }
}
