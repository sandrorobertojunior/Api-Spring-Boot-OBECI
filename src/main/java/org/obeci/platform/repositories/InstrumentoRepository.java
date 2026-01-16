package org.obeci.platform.repositories;

import org.obeci.platform.entities.Instrumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
/**
 * Repositório JPA para {@link Instrumento}.
 *
 * <p>Garante lookup por turma (constraint de unicidade em {@code turma_id}).</p>
 */
public interface InstrumentoRepository extends JpaRepository<Instrumento, Long> {
    Optional<Instrumento> findByTurmaId(Long turmaId);
    boolean existsByTurmaId(Long turmaId);
}
