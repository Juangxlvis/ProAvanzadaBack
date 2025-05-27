package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

public record PreguntaBancoDTO(
        Integer id_pregunta,
        String enunciado,
        Character es_publica,
        String tipo_pregunta,
        Integer id_tema,
        Integer id_docente
) {
}
