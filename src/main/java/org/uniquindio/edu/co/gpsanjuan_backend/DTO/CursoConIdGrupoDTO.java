package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

public record CursoConIdGrupoDTO(
        Integer id_curso,
        String nombre_curso,
        Integer id_grupo // ID del grupo al que el usuario pertenece para este curso (puede ser null)
) {}