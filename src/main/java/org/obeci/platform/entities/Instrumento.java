package org.obeci.platform.entities;

import jakarta.persistence.*;
import jakarta.persistence.Basic;
import jakarta.persistence.FetchType;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "instrumentos", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"turma_id"})
})
@EntityListeners(AuditingEntityListener.class)
public class Instrumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "turma_id", nullable = false)
    private Long turmaId;

    // JSON dos slides conforme modelo do front-end
    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(name = "slides_json", nullable = false, columnDefinition = "TEXT")
    private String slidesJson;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime dataModificacao = LocalDateTime.now();
}
