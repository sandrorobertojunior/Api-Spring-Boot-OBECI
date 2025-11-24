package org.obeci.platform.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "turmas")
@EntityListeners(AuditingEntityListener.class)
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
