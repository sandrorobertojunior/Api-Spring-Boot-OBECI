package org.obeci.platform.repositories;

import org.obeci.platform.entities.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Long> {
    List<Turma> findByEscolaId(Long escolaId);
    List<Turma> findByProfessorId(Long professorId);
    List<Turma> findByIsActive(Boolean isActive);
}