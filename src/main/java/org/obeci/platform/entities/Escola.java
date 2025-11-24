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
@Table(name = "escolas")
@EntityListeners(AuditingEntityListener.class)
public class Escola {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Long direcaoId;

    @Column(nullable = false)
    private String endereco;

    @Column(nullable = false)
    private String numero;

    @Column(nullable = false)
    private Boolean isActive;

    public Escola() {

    }

    public Escola(String nome, Long direcaoId, String endereco, String numero, Boolean isActive) {
        this();
        this.nome = nome;
        this.direcaoId = direcaoId;
        this.endereco = endereco;
        this.numero = numero;
        this.isActive = isActive;
    }
}
