package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.util.List;

public record CursoGruposDTO(
        Integer id_curso,
        String nombre_curso,
        String descripcion_curso,
        List<GrupoSimpleDTO> gruposDelUsuario
) {}
