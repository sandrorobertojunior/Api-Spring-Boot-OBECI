package org.obeci.platform.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InstrumentoDto {
    private Long id;
    private Long turmaId;
    private String slidesJson;
}
