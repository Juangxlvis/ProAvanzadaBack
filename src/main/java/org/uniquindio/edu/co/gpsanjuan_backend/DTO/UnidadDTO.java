package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

public record UnidadDTO(
        Long unidadId,

        String titulo,

        String descripcion,
        CursoDTO curso
) {
}
