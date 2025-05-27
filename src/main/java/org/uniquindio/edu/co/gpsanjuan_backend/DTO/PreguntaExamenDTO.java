package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

public record PreguntaExamenDTO(

        Integer preguntaExamenId,
        Double porcentajeExamen,

        Integer tiempoPregunta,

        Character tieneTiempoMaximo,

        Integer id_pregunta,

        Integer id_examen
) {
}
