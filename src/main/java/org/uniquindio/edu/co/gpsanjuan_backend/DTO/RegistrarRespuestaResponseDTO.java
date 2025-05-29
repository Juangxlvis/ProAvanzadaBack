package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

public record RegistrarRespuestaResponseDTO(
        Integer idPresentacionPregunta, // El ID del registro creado en PRESENTACION_PREGUNTA
        String mensaje,
        Boolean fueCorrecta // 'T' o 'F' del PL/SQL convertido a Boolean
) {}