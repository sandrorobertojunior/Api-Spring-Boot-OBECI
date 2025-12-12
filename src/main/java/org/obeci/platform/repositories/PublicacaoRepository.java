package org.obeci.platform.repositories;

import org.obeci.platform.entities.Publicacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PublicacaoRepository extends JpaRepository<Publicacao, Long> {
    List<Publicacao> findByTurmaId(Long turmaId);
    List<Publicacao> findByEscolaId(Long escolaId);
    List<Publicacao> findByIsPublic(Boolean isPublic);
    List<Publicacao> findByUsername(String username);
}