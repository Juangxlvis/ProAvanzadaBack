package org.uniquindio.edu.co.gpsanjuan_backend.services.interfaces;

import jakarta.transaction.Transactional;
import org.uniquindio.edu.co.gpsanjuan_backend.DTO.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface AlumnoService {
    String guardarPregunta(PreguntaDTO preguntaDTO);



    Float obtenerNotaPresentacionExamen (Integer id_presentacion_examen);

    String obtenerNombre(String id, String rol);

    String crearPresentacionExamen (Integer tiempo, Character terminado, String ip_source,
                                    LocalDate fecha_hora_presentacion, Integer id_examen, Integer id_alumno );

    List<CursoSimpleDTO> obtenerCursos(String id, String rol);

    List<ExamenPendienteDTO> obtenerExamenesPendiente(String id, Integer idGrupo);

    List<ExamenHechoDTO> obtenerExamenesHechos(String id, Integer idGrupo);


    String responderPregunta(Integer idPresentacionExamen,
                             Integer idPregunta,
                             Integer idRespuesta);

    String finalizarPresentacionExamen(Integer idPresentacionExamen);

    ExamenParaPresentarDTO iniciarCargarExamenParaPresentar(Integer idAlumno, Integer idExamen, String ipCliente);

    RegistrarRespuestaResponseDTO registrarRespuestaAlumno(
            Integer idPresentacionExamen, // Viene de la presentación actual
            Integer idAlumno,            // Para validación, del usuario autenticado
            RegistrarRespuestaRequestDTO respuestaData
    );

    FinalizarExamenResponseDTO finalizarPresentacionExamen(Integer idPresentacionExamen, Integer idAlumno);

    CursoGruposDTO obtenerCursoConGruposParaUsuario(String idUsuario, String rol, Integer idCurso);

    List<CursoConIdGrupoDTO> obtenerCursosAlumno(String id, String rol);

}
