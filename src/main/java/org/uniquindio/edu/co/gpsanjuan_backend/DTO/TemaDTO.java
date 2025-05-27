package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.util.List;

public record TemaDTO (
        Integer temaId,
        String titulo,

        String descripcion,

        List<ExamenDTO> examenes,

        Integer unidad_id_unidad
){
}
