package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.util.List;

public record GrupoDTO(
        Integer grupoId,
        String jornada,
        String nombre,
        String periodo,
        List<NotaDTO> notas,
        Integer id_curso,
        List<BloqueHorarioDTO> horarios,
        List<AlumnoDTO> alumnos,
        Integer id_docente
) {

}
