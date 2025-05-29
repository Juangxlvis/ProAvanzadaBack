package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

public record RegistrarRespuestaRequestDTO(
        Integer idPregunta,
        Integer idRespuestaSeleccionada // ID de la opción que el alumno eligió
        // String textoRespuesta; // Descomenta y añade si tienes preguntas abiertas
) {}
