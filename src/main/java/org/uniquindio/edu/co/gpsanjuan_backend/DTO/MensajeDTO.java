package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

public record MensajeDTO<T>(
        Boolean error,
        String mensajeError,
        T respuesta
) {
}
