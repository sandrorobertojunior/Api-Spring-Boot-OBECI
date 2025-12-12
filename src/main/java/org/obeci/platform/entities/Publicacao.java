package org.obeci.platform.entities;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
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
@Table(name = "publicacoes")
@EntityListeners(AuditingEntityListener.class)
public class Publicacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Long turmaId;

    @Column(nullable = false)
    private Long escolaId;

    @Column(nullable = false)
    private Boolean isPublic;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "hashtags", columnDefinition = "text[]")
    private List<String> hashtags = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime dataModificacao = LocalDateTime.now();

    public Publicacao() {

    }

    public Publicacao(Long id, Long turmaId, Long escolaId, Boolean isPublic, String title, String content, List<String> hashtags, LocalDateTime dataModificacao) {
        this();
        this.id = id;
        this.turmaId = turmaId;
        this.escolaId = escolaId;
        this.isPublic = isPublic;
        this.title = title;
        this.content = content;
        this.hashtags = hashtags;
        this.dataModificacao = dataModificacao;
    }
}