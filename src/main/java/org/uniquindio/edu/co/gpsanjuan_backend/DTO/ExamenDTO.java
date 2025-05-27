package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public record ExamenDTO(
        Integer id_examen,

        Integer tiempo_maximo,
        Integer numero_preguntas,

        Float porcentaje_curso,

        String nombre,

        String descripcion,

        Integer porcentaje_aprobatorio,

        Date fecha_hora_inicio,

        Date fecha_hora_limite,

        Integer numero_preguntas_aleatorias,

        Integer grupo_id,

        Integer docente_id,

        // List<PresentacionExamenDTO> examenesPresentados --Tampoco creo necesario esto
        Integer tema_id,
        String estado

        //List<PreguntaExamenDTO> preguntasExamen
) {
}
