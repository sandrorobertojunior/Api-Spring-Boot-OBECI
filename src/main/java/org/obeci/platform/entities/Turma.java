package org.obeci.platform.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
@Data
@Entity
@Table(name = "turmas")
@EntityListeners(AuditingEntityListener.class)
/**
 * Entidade JPA que representa uma turma.
 *
 * <p>Mapeamento atual usa IDs primitivos ({@code escolaId}, {@code professorId}) em vez de
 * relacionamentos JPA (ManyToOne). Isso simplifica o modelo, mas delega joins/consistência
 * para a camada de serviço ou consultas específicas.</p>
 */
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long escolaId;

    @Column(nullable = false)
    private Long professorId;

    @Column(nullable = false)
    private String turno;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Boolean isActive;

    public Turma() {

    }

    public Turma(Long escolaId, Long professorId, String turno, String nome, Boolean isActive) {
        this();
        this.escolaId = escolaId;
        this.professorId = professorId;
        this.turno = turno;
        this.nome = nome;
        this.isActive = isActive;
    }
}
