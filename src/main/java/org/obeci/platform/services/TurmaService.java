package org.obeci.platform.services;

import org.obeci.platform.entities.Turma;
import org.obeci.platform.repositories.TurmaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TurmaService {

    @Autowired
    private TurmaRepository turmaRepository;

    @Autowired
    private InstrumentoService instrumentoService;

    public List<Turma> getAllTurmas() {
        return turmaRepository.findAll();
    }

    public Optional<Turma> getTurmaById(Long id) {
        return turmaRepository.findById(id);
    }

    public Turma createTurma(Turma turma) {
        Turma saved = turmaRepository.save(turma);
        // Cria automaticamente um Instrumento vazio para a turma
        try {
            instrumentoService.createEmptyForTurma(saved.getId());
        } catch (Exception ignored) {
        }
        return saved;
    }

    public Turma updateTurma(Long id, Turma turmaDetails) {
        Turma turma = turmaRepository.findById(id).orElse(null);
        if (turma != null) {
            turma.setEscolaId(turmaDetails.getEscolaId());
            turma.setProfessorId(turmaDetails.getProfessorId());
            turma.setTurno(turmaDetails.getTurno());
            turma.setNome(turmaDetails.getNome());
            turma.setIsActive(turmaDetails.getIsActive());
            return turmaRepository.save(turma);
        }
        return null;
    }

    public boolean deleteTurma(Long id) {
        if (turmaRepository.existsById(id)) {
            turmaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Turma> getTurmasByEscolaId(Long escolaId) {
        return turmaRepository.findByEscolaId(escolaId);
    }

    public List<Turma> getTurmasByProfessorId(Long professorId) {
        return turmaRepository.findByProfessorId(professorId);
    }

    public List<Turma> getActiveTurmas(Boolean isActive) {
        return turmaRepository.findByIsActive(isActive);
    }
}