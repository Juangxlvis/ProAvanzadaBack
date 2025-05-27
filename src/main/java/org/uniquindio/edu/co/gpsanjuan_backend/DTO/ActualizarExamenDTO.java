package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.util.Date;

public record ActualizarExamenDTO(
        Integer idExamen,
        Integer tiempoMax,
        Integer numeroPreguntas,
        Integer porcentajeCurso,
        String nombre,
        String descripcion,
        Float porcentajeAprobatorio,
        Date fechaHoraInicio,
        Date fechaHoraFin,
        String estado
) {}
