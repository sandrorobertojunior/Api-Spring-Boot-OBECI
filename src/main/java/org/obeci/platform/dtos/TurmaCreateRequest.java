package org.obeci.platform.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TurmaCreateRequest {
    @NotNull(message = "escolaId é obrigatório")
    private Long escolaId;

    @NotNull(message = "professorId é obrigatório")
    private Long professorId;

    @NotBlank(message = "Turno é obrigatório")
    private String turno;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotNull(message = "isActive é obrigatório")
    private Boolean isActive;

    public Long getEscolaId() { return escolaId; }
    public void setEscolaId(Long escolaId) { this.escolaId = escolaId; }

    public Long getProfessorId() { return professorId; }
    public void setProfessorId(Long professorId) { this.professorId = professorId; }

    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
