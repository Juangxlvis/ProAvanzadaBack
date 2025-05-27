package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.util.List;

public record PreguntaDTO (

    String enunciado,

    Character es_publica,
    String tipo_pregunta,

    Integer id_docente,
    Integer id_tema
){
}
