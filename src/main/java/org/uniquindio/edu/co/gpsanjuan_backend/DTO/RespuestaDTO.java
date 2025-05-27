package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.util.List;

public record RespuestaDTO (

        Integer respuestaId,
        String descripcion,
        Character esVerdadera,

        Integer id_pregunta,

        List<PresentacionPreguntaDTO> presentacionesPregunta

){
}
