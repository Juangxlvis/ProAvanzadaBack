package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

public record PresentacionPreguntaDTO (

        Integer presentacionPreguntaId,

        Character respuestaCorrecta,

        Integer idPresentacionExamen,

        Integer id_pregunta,

        Integer id_respuesta
){
}
