package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.util.List;

public record PreguntaExamenDTO(

        Integer id_pregunta,       // JSON key: "id_pregunta"
        String enunciado,          // JSON key: "enunciado"
        String tipo_pregunta,      // JSON key: "tipo_pregunta"
        Double porcentajeExamen,   // JSON key: "porcentajeExamen" (Nota: PL/SQL NUMBER puede ser Double o BigDecimal)
        Integer tiempoPregunta,    // JSON key: "tiempoPregunta"
        Character tieneTiempoMaximo,  // JSON key: "tieneTiempoMaximo" (CHAR(1) en BD puede ser String o Character en Java)
        Integer id_examen,         // JSON key: "id_examen"
        Integer preguntaExamenId,  // JSON key: "preguntaExamenId"
        List<OpcionRespuestaDTO> opciones // JSON key: "opciones"
) {
}
