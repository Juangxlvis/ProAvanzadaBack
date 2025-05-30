package org.uniquindio.edu.co.gpsanjuan_backend.services.implementations;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.uniquindio.edu.co.gpsanjuan_backend.DTO.*;
import org.uniquindio.edu.co.gpsanjuan_backend.services.interfaces.AlumnoService;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
@Service
@AllArgsConstructor
public class AlumnoServiceImp implements AlumnoService {

    private final EntityManager entityManager;


    @Transactional
    public String guardarPregunta(PreguntaDTO preguntaDTO) {
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("crear_pregunta");


        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("v_enunciado", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_es_publica", Character.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_tipo_pregunta", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_id_tema", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_id_docente", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_mensaje", String.class, ParameterMode.OUT);


        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("v_enunciado", preguntaDTO.enunciado());
        storedProcedure.setParameter("v_es_publica", preguntaDTO.es_publica());
        storedProcedure.setParameter("v_tipo_pregunta", preguntaDTO.tipo_pregunta());
        storedProcedure.setParameter("v_id_tema", preguntaDTO.id_tema());
        storedProcedure.setParameter("v_id_docente", preguntaDTO.id_docente());

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        String mensaje = (String) storedProcedure.getOutputParameterValue("v_mensaje");

        return mensaje;
    }
    @Transactional
    @Override
    public Float obtenerNotaPresentacionExamen(Integer id_presentacion_examen) {
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("obtener_nota");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("v_id_presentacion_examen", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_nota", Float.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("v_id_presentacion_examen", id_presentacion_examen);

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        // Obtener el valor del parámetro de salida
        Float nota = (Float) storedProcedure.getOutputParameterValue("v_nota");

        // Retornar la nota
        return nota != null ? nota : 0.0f;
    }

    @Override
    public String obtenerNombre(String id, String rol) {
        // Crear una consulta para el procedimiento almacenado
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("get_nombre_usuario");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("p_id_usuario", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("rol", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("res", String.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("p_id_usuario", id);
        storedProcedure.setParameter("rol", "alumno");

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        String nombre = (String) storedProcedure.getOutputParameterValue("res");

        return nombre;
    }

    @Transactional
    @Override
    public String crearPresentacionExamen(Integer tiempo, Character terminado, String ip_source, LocalDate fecha_hora_presentacion, Integer id_examen, Integer id_alumno) {
        // Crear una consulta para el procedimiento almacenado
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("crear_presentacion_examen");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("v_tiempo", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_terminado", Character.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_ip", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_fecha_hora_presentacion", Date.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_id_examen", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_id_alumno", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_mensaje", String.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("v_tiempo", tiempo);
        storedProcedure.setParameter("v_terminado", "N"); // Establece el valor predeterminado para 'terminado'
        storedProcedure.setParameter("v_ip", ip_source);
        storedProcedure.setParameter("v_fecha_hora_presentacion", fecha_hora_presentacion);
        storedProcedure.setParameter("v_id_examen", id_examen);
        storedProcedure.setParameter("v_id_alumno", id_alumno);

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        // Obtener el valor del parámetro de salida
        String mensaje = (String) storedProcedure.getOutputParameterValue("v_mensaje");

        // Retornar el mensaje
        return mensaje;
    }

    @Override
    public List<CursoSimpleDTO> obtenerCursos(String id, String rol) {
        // Crear una consulta para el procedimiento almacenado
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("get_cursos_usuario");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("p_id_usuario", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("rol", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("res", String.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("p_id_usuario", id);
        storedProcedure.setParameter("rol", "alumno");

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        String json1 = (String) storedProcedure.getOutputParameterValue("res");

        Gson gson = new Gson();
        Type personListType = new TypeToken<List<CursoSimpleDTO>>() {}.getType();

        return gson.fromJson(json1, personListType);
    }

    @Override
    public List<ExamenPendienteDTO> obtenerExamenesPendiente(String id, Integer idGrupo) {

        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("get_examenes_grupo_pendientes_por_alumno");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("p_id_alumno", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("p_id_grupo", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("res", String.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("p_id_alumno", Integer.parseInt(id));
        storedProcedure.setParameter("p_id_grupo", idGrupo);

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        String json1 = (String) storedProcedure.getOutputParameterValue("res");
        System.out.println("JSON from DB for get_examenes_pendientes (ID " + id + ", ID Grupo " + idGrupo + "): |" + json1 + "|");
        Gson gson = new Gson();
        Type personListType = new TypeToken<List<ExamenPendienteDTO>>() {}.getType();
        return gson.fromJson(json1, personListType);    }

    @Override
    public List<ExamenHechoDTO> obtenerExamenesHechos(String id, Integer idGrupo) {
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("GET_PRESENTACION_EXAMEN_ALUMNO_GRUPO");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("p_id_alumno", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("p_id_grupo", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("res", String.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("p_id_alumno", Integer.parseInt(id));
        storedProcedure.setParameter("p_id_grupo", idGrupo);

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        String json1 = (String) storedProcedure.getOutputParameterValue("res");
        Gson gson = new Gson();
        Type personListType = new TypeToken<List<ExamenHechoDTO>>() {}.getType();
        return gson.fromJson(json1, personListType);
    }
    @Transactional
    @Override
    public String responderPregunta(Integer idPresentacionExamen,
                                    Integer idPregunta,
                                    Integer idRespuesta) {
        StoredProcedureQuery sp = entityManager
                .createStoredProcedureQuery("responder_pregunta");

        // parámetros IN
        sp.registerStoredProcedureParameter("p_id_presentacion_examen", Integer.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("p_id_pregunta",            Integer.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("p_id_respuesta",           Integer.class, ParameterMode.IN);
        // parámetro OUT de mensaje
        sp.registerStoredProcedureParameter("p_mensaje",                String.class,  ParameterMode.OUT);

        sp.setParameter("p_id_presentacion_examen", idPresentacionExamen);
        sp.setParameter("p_id_pregunta",            idPregunta);
        sp.setParameter("p_id_respuesta",           idRespuesta);

        sp.execute();
        return (String) sp.getOutputParameterValue("p_mensaje");
    }

    /**
     * 2) finalizarPresentacionExamen: marca terminada la presentación y calcula nota final
     */
    @Transactional
    @Override
    public String finalizarPresentacionExamen(Integer idPresentacionExamen) {
        StoredProcedureQuery sp = entityManager
                .createStoredProcedureQuery("finalizar_presentacion_examen");

        // parámetro IN
        sp.registerStoredProcedureParameter("p_id_presentacion_examen", Integer.class, ParameterMode.IN);
        // parámetro OUT
        sp.registerStoredProcedureParameter("p_mensaje",                String.class,  ParameterMode.OUT);

        sp.setParameter("p_id_presentacion_examen", idPresentacionExamen);

        sp.execute();
        return (String) sp.getOutputParameterValue("p_mensaje");
    }

    @Override
    @Transactional // Puede ser importante si el PL/SQL realiza inserciones y otras operaciones
    public ExamenParaPresentarDTO iniciarCargarExamenParaPresentar(Integer idAlumno, Integer idExamen, String ipCliente) {
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("iniciar_y_cargar_examen");

        // Registrar parámetros IN
        storedProcedure.registerStoredProcedureParameter("p_id_alumno_in", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("p_id_examen_in", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("p_ip_source_in", String.class, ParameterMode.IN);

        // Registrar parámetros OUT
        storedProcedure.registerStoredProcedureParameter("p_id_presentacion_creada", Integer.class, ParameterMode.OUT);
        storedProcedure.registerStoredProcedureParameter("p_examen_json_out", String.class, ParameterMode.OUT); // CLOB se mapea a String
        storedProcedure.registerStoredProcedureParameter("p_mensaje_out", String.class, ParameterMode.OUT);

        // Establecer valores de parámetros IN
        storedProcedure.setParameter("p_id_alumno_in", idAlumno);
        storedProcedure.setParameter("p_id_examen_in", idExamen);
        storedProcedure.setParameter("p_ip_source_in", ipCliente);

        try {
            storedProcedure.execute();

            // Obtener valores de los parámetros OUT
            String mensajeOut = (String) storedProcedure.getOutputParameterValue("p_mensaje_out");
            // Integer idPresentacionCreada = (Integer) storedProcedure.getOutputParameterValue("p_id_presentacion_creada"); // Ya viene en el JSON
            String jsonExamen = (String) storedProcedure.getOutputParameterValue("p_examen_json_out");

            // Verificar si el procedimiento PL/SQL reportó un error de lógica de negocio
            if (mensajeOut != null && mensajeOut.toLowerCase().startsWith("error:")) {
                // Puedes loggear 'mensajeOut' y/o 'idPresentacionCreada' (que sería null) si es útil
                System.err.println("Error desde PL/SQL (iniciar_y_cargar_examen): " + mensajeOut);
                throw new RuntimeException(mensajeOut); // Lanza una excepción que el controlador puede atrapar
            }

            // Verificar si el JSON del examen es nulo o vacío (no debería si mensajeOut fue exitoso)
            if (jsonExamen == null || jsonExamen.trim().isEmpty()) {
                System.err.println("PL/SQL (iniciar_y_cargar_examen) no devolvió JSON del examen, mensaje: " + mensajeOut);
                throw new RuntimeException("No se recibieron los detalles del examen desde la base de datos.");
            }

            // Deserializar el JSON a ExamenParaPresentarDTO
            // Si tus DTOs de respuesta (ExamenParaPresentarDTO, PreguntaExamenDTO, OpcionRespuestaDTO)
            // contienen campos como LocalDate, LocalDateTime, etc., necesitarás configurar Gson con adaptadores.
            GsonBuilder gsonBuilder = new GsonBuilder();
            // Ejemplo si alguno de tus DTOs usa LocalDate:
            // gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
            // Añade más adaptadores si son necesarios para otros tipos de java.time

            Gson gson = gsonBuilder.create();

            ExamenParaPresentarDTO examenParaPresentar = gson.fromJson(jsonExamen, ExamenParaPresentarDTO.class);

            if (examenParaPresentar == null) { // Chequeo extra
                throw new RuntimeException("Falló la deserialización del JSON del examen.");
            }

            return examenParaPresentar;

        } catch (RuntimeException re) { // Re-lanzar excepciones de lógica de negocio o errores ya formateados
            throw re;
        } catch (Exception e) {
            // Loggear el error completo para diagnóstico
            System.err.println("Excepción en Java al llamar a iniciar_y_cargar_examen: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error interno del servidor al procesar la solicitud para iniciar el examen.", e);
        }
    }

    @Override
    @Transactional
    public RegistrarRespuestaResponseDTO registrarRespuestaAlumno(
            Integer idPresentacionExamen, Integer idAlumno, RegistrarRespuestaRequestDTO respuestaData) {

        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("registrar_respuesta_alumno");

        storedProcedure.registerStoredProcedureParameter("p_id_presentacion_examen_in", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("p_id_pregunta_in", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("p_id_respuesta_seleccionada_in", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("p_id_alumno_in", Integer.class, ParameterMode.IN);

        storedProcedure.registerStoredProcedureParameter("p_id_presentacion_pregunta_out", Integer.class, ParameterMode.OUT);
        storedProcedure.registerStoredProcedureParameter("p_respuesta_fue_correcta_out", Character.class, ParameterMode.OUT); // CHAR(1)
        storedProcedure.registerStoredProcedureParameter("p_mensaje_out", String.class, ParameterMode.OUT);
        storedProcedure.registerStoredProcedureParameter("p_error_out", String.class, ParameterMode.OUT);

        storedProcedure.setParameter("p_id_presentacion_examen_in", idPresentacionExamen);
        storedProcedure.setParameter("p_id_pregunta_in", respuestaData.idPregunta());
        storedProcedure.setParameter("p_id_respuesta_seleccionada_in", respuestaData.idRespuestaSeleccionada());
        storedProcedure.setParameter("p_id_alumno_in", idAlumno);

        try {
            storedProcedure.execute();

            String errorOut = (String) storedProcedure.getOutputParameterValue("p_error_out");
            if (errorOut != null && !errorOut.isBlank()) {
                throw new RuntimeException(errorOut);
            }

            String mensajeOut = (String) storedProcedure.getOutputParameterValue("p_mensaje_out");
            Integer idPresentacionPregunta = (Integer) storedProcedure.getOutputParameterValue("p_id_presentacion_pregunta_out");
            Character respuestaCorrectaChar = (Character) storedProcedure.getOutputParameterValue("p_respuesta_fue_correcta_out");

            if (mensajeOut == null || (mensajeOut.toLowerCase().startsWith("error:") && !mensajeOut.toLowerCase().contains("ya existe una respuesta"))) {
                // El chequeo de "ya existe" es un caso especial que podría no ser un error fatal si se permite actualizar.
                // Pero si es otro error, o mensaje nulo, es un problema.
                throw new RuntimeException(mensajeOut != null ? mensajeOut : "Error desconocido al registrar la respuesta.");
            }

            Boolean fueCorrecta = null;
            if (respuestaCorrectaChar != null) {
                fueCorrecta = (respuestaCorrectaChar == 'T' || respuestaCorrectaChar == 'V');
            }

            return new RegistrarRespuestaResponseDTO(idPresentacionPregunta, mensajeOut, fueCorrecta);

        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error interno del servidor al registrar la respuesta: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional // Importante para la actualización del estado del examen
    public FinalizarExamenResponseDTO finalizarPresentacionExamen(Integer idPresentacionExamen, Integer idAlumno) {
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("finalizar_presentacion_examen");

        // Parámetros IN
        storedProcedure.registerStoredProcedureParameter("p_id_presentacion_examen_in", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("p_id_alumno_in", Integer.class, ParameterMode.IN);

        // Parámetros OUT
        storedProcedure.registerStoredProcedureParameter("p_mensaje_out", String.class, ParameterMode.OUT);
        storedProcedure.registerStoredProcedureParameter("p_calificacion_final_out", BigDecimal.class, ParameterMode.OUT); // NUMBER de Oracle
        storedProcedure.registerStoredProcedureParameter("p_error_out", String.class, ParameterMode.OUT);

        // Establecer valores de parámetros IN
        storedProcedure.setParameter("p_id_presentacion_examen_in", idPresentacionExamen);
        storedProcedure.setParameter("p_id_alumno_in", idAlumno);

        try {
            storedProcedure.execute();

            String mensajeOut = (String) storedProcedure.getOutputParameterValue("p_mensaje_out");
            BigDecimal calificacionOut = (BigDecimal) storedProcedure.getOutputParameterValue("p_calificacion_final_out");
            String errorOut = (String) storedProcedure.getOutputParameterValue("p_error_out");

            // Verificar si el PL/SQL reportó un error de lógica de negocio
            if (errorOut != null && !errorOut.isBlank()) {
                System.err.println("Error desde PL/SQL (finalizar_presentacion_examen): " + errorOut);
                throw new RuntimeException(errorOut);
            }
            // Doble chequeo por si p_error_out fue null pero p_mensaje_out indica error
            if (mensajeOut == null || mensajeOut.toLowerCase().startsWith("error:")) {
                System.err.println("Error o mensaje inesperado desde PL/SQL (finalizar_presentacion_examen): " + mensajeOut);
                throw new RuntimeException(mensajeOut != null ? mensajeOut : "Error desconocido al finalizar el examen.");
            }

            String estadoPresentacion = "Finalizado";
            if (calificacionOut != null) {
                estadoPresentacion = "Calificado";
            } else if (mensajeOut.toLowerCase().contains("calificación: pendiente")) {
                estadoPresentacion = "Finalizado - Pendiente de Calificación";
            }

            return new FinalizarExamenResponseDTO(mensajeOut, calificacionOut, estadoPresentacion);

        } catch (RuntimeException re) { // Re-lanzar excepciones ya formateadas (de lógica de negocio)
            throw re;
        } catch (Exception e) {
            System.err.println("Excepción en Java al llamar a finalizar_presentacion_examen: " + e.getMessage());
            e.printStackTrace(); // Loguear el stack trace completo
            throw new RuntimeException("Error interno del servidor al procesar la finalización del examen.", e);
        }
    }

    @Override
    public CursoGruposDTO obtenerCursoConGruposParaUsuario(String idUsuario, String rol, Integer idCurso) {
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("get_curso_con_grupos_usuario");

        storedProcedure.registerStoredProcedureParameter("p_id_usuario_in", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("p_rol_in", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("p_id_curso_seleccionado_in", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("res_json_out", String.class, ParameterMode.OUT); // CLOB se mapea a String
        storedProcedure.registerStoredProcedureParameter("p_error_out", String.class, ParameterMode.OUT);

        storedProcedure.setParameter("p_id_usuario_in", idUsuario);
        storedProcedure.setParameter("p_rol_in", rol);
        storedProcedure.setParameter("p_id_curso_seleccionado_in", idCurso);

        try {
            storedProcedure.execute();

            String errorOut = (String) storedProcedure.getOutputParameterValue("p_error_out");
            if (errorOut != null && !errorOut.isBlank()) {
                System.err.println("Error desde PL/SQL (get_curso_con_grupos_usuario): " + errorOut);
                throw new RuntimeException(errorOut); // Lanza excepción para que el controlador la maneje
            }

            String jsonResult = (String) storedProcedure.getOutputParameterValue("res_json_out");
            if (jsonResult == null || jsonResult.trim().isEmpty()) {
                System.err.println("PL/SQL (get_curso_con_grupos_usuario) no devolvió JSON. Error PL/SQL: " + errorOut);
                // Esto podría ocurrir si p_error_out se pobló y el JSON no se construyó.
                // La excepción anterior ya debería haber sido lanzada.
                throw new RuntimeException("No se recibieron detalles del curso y grupos desde la base de datos.");
            }

            GsonBuilder gsonBuilder = new GsonBuilder();
            // Registrar adaptadores si CursoConGruposDTO o GrupoSimpleUsuarioDTO tuvieran tipos complejos como LocalDate
            Gson gson = gsonBuilder.create();

            return gson.fromJson(jsonResult, CursoGruposDTO.class);

        } catch (RuntimeException re) {
            throw re; // Re-lanzar excepciones de lógica de negocio
        } catch (Exception e) {
            System.err.println("Excepción en Java al llamar a get_curso_con_grupos_usuario: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error interno del servidor al obtener detalles del curso y grupos.", e);
        }
    }

    @Override
    public List<CursoConIdGrupoDTO> obtenerCursosAlumno(String id, String rol) { // Cambiado el tipo de retorno
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("GET_CURSOS_GRUPO_ALUMNO");

        storedProcedure.registerStoredProcedureParameter("p_id_usuario_in", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("p_rol_in", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("res_json_out", String.class, ParameterMode.OUT);
        storedProcedure.registerStoredProcedureParameter("p_error_out", String.class, ParameterMode.OUT);

        storedProcedure.setParameter("p_id_usuario_in", id);
        storedProcedure.setParameter("p_rol_in", rol);

        try {
            storedProcedure.execute();

            String errorOut = (String) storedProcedure.getOutputParameterValue("p_error_out");
            if (errorOut != null && !errorOut.isBlank()) {
                System.err.println("Error desde PL/SQL (get_cursos_usuario): " + errorOut);
                throw new RuntimeException(errorOut);
            }

            String jsonResult = (String) storedProcedure.getOutputParameterValue("res_json_out");

            if (jsonResult == null || jsonResult.trim().isEmpty() ||
                    (jsonResult.trim().startsWith("{") && jsonResult.toLowerCase().contains("\"error_exception\":"))) {
                System.err.println("Respuesta vacía o error JSON desde PL/SQL (get_cursos_usuario). JSON: " + jsonResult);
                if (jsonResult != null && jsonResult.trim().equals("[]") && (errorOut == null || errorOut.isBlank())) {
                    return Collections.emptyList();
                }
                throw new RuntimeException("No se recibieron datos de cursos válidos desde la base de datos.");
            }

            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();

            Type listType = new TypeToken<List<CursoConIdGrupoDTO>>() {}.getType(); // Usar el nuevo DTO
            List<CursoConIdGrupoDTO> cursos = gson.fromJson(jsonResult, listType);

            if (cursos == null && (errorOut == null || errorOut.isBlank())) {
                System.err.println("Deserialización de cursos resultó en NULL. JSON: " + jsonResult);
                return Collections.emptyList();
            }
            return cursos;

        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            System.err.println("Excepción en Java al llamar a get_cursos_usuario: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error interno del servidor al obtener la lista de cursos.", e);
        }
    }
}
