package org.uniquindio.edu.co.gpsanjuan_backend.DTO;

import java.time.LocalDate;
import java.util.List;

public record PresentacionExamenDTO (
        Integer id_presentacion_examen,
        Integer tiempo,
        Character presentado,
        String  ipSource,
        LocalDate fechaHoraPresentacion,
        Integer id_alumno,
        Integer id_examen
){
}
