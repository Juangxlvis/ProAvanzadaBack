package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.util.List;

public record AlumnoDTO(
        String nombre,
        String apellido,
        Integer alumnoId,

        List<GrupoDTO> grupos,

        List<NotaDTO> notas,

        List<PresentacionExamenDTO> examenes
) {
}
