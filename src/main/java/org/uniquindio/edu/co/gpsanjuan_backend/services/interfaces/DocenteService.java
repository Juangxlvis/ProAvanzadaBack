package org.uniquindio.edu.co.gpsanjuan_backend.services.interfaces;

import org.uniquindio.edu.co.gpsanjuan_backend.DTO.*;

import java.text.ParseException;
import java.util.List;

public interface DocenteService {

        List<PreguntaBancoDTO> obtenerBancoPreguntas (Integer id_tema);

    String crearRespuesta (String descripcion, Character esVerdadera, Integer id_pregunta );

    String crearExamen (CrearExamenDTO examenDTO) throws ParseException;

    CrearPreguntaResponseDTO crearPregunta(String enunciado, Character esPublica, String tipoPregunta, Integer idTema, Integer idDocente);


    String calificarExamen (Integer id_presentacion_examen);

    List <PreguntaBancoDTO> obtenerPreguntasDocente(Integer id_docente);

    List <ExamenDTO> obtenerExamenesDocente (Integer id_docente);

    String obtenerNombre(String id, String rol);

    List<CursoSimpleDTO> obtenerCursos(String id, String rol);

    List<TemasCursoDTO> obtenerTemasCurso(Integer id_curso);

    List<TemasCursoDTO> obtenerTemasDocente();

    List<GrupoSimpleDTO> obtenerGruposPorCurso(Integer idCurso, Integer idDocente);


}
