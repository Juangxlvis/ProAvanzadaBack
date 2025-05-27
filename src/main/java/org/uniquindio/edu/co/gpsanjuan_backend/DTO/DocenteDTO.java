package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.util.List;

public record DocenteDTO(
        Integer docenteId,
        String nombre,
        String apellido,
        List<GrupoDTO> grupos,
        List<ExamenDTO> examenes,
        List<PreguntaDTO> preguntas
) {
}
