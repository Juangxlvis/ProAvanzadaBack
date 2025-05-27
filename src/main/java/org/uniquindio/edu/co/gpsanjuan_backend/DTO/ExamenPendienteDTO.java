package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.time.LocalDateTime;

public record ExamenPendienteDTO(
        Integer id_examen,
        Integer tiempo_maximo,
        Integer numero_preguntas,
        Integer porcentaje_aprobatorio,
        String nombre,
        Integer porcentaje_curso,
        String fecha_hora_inicio,
        String fecha_hora_fin,
        String tema
) {
}
