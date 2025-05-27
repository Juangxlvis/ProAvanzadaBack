package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.util.List;

public record ExamenParaPresentarDTO(
        Integer id_presentacion_examen, // El ID del intento actual de presentación
        Integer id_examen,              // El ID del examen en sí
        String nombre_examen,
        String descripcion_examen,
        Integer tiempo_maximo,          // En minutos, por ejemplo
        Integer numero_preguntas_totales,
        List<PreguntaExamenDTO> preguntas // La lista de preguntas del examen
) {}