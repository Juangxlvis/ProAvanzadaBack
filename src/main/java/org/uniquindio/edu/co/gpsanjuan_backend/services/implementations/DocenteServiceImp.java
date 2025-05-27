package org.uniquindio.edu.co.gpsanjuan_backend.services.implementations;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import oracle.jdbc.internal.OracleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.uniquindio.edu.co.gpsanjuan_backend.DTO.*;
import org.uniquindio.edu.co.gpsanjuan_backend.repositories.ExamenRepository;
import org.uniquindio.edu.co.gpsanjuan_backend.services.interfaces.DocenteService;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DocenteServiceImp implements DocenteService {

    private final EntityManager entityManager;
    @Autowired
    private final ExamenRepository examenRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public List<PreguntaBancoDTO> obtenerBancoPreguntas(Integer id_tema) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("obtener_banco_preguntas")
                .declareParameters(
                        new SqlParameter("v_id_tema", OracleTypes.NUMBER),
                        new SqlOutParameter("p_preguntas", OracleTypes.CURSOR)
                )
                .returningResultSet("p_preguntas", this::mapRowToPreguntaBancoDTO);

        Map<String, Object> result = jdbcCall.execute(new MapSqlParameterSource("v_id_tema", id_tema));

        System.out.println("llega hasta antes del return dentro del servicio docente");

        return (List<PreguntaBancoDTO>) result.get("p_preguntas");
    }
    @Transactional
    @Override
    public String crearRespuesta(String descripcion, Character esVerdadera, Integer id_pregunta) {

        // Crear una consulta para el procedimiento almacenado
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("crear_respuesta");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("v_descripcion", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_es_verdadera", Character.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_id_pregunta", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_mensaje", String.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("v_descripcion", descripcion);
        storedProcedure.setParameter("v_es_verdadera", esVerdadera);
        storedProcedure.setParameter("v_id_pregunta", id_pregunta);

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        // Obtener el valor del parámetro de salida
        String mensaje = (String) storedProcedure.getOutputParameterValue("v_mensaje");

        // Retornar el mensaje
        return mensaje;
    }

    @Override
    @Transactional
    public String crearExamen(CrearExamenDTO examenDTO) throws ParseException {
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("crear_examen");

        storedProcedure.registerStoredProcedureParameter("v_tiempo_max", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_numero_preguntas", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_porcentaje_curso", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_nombre", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_descripcion", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_porcentaje_aprobatorio", Float.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("p_fecha_inicio_str", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("p_fecha_fin_str", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_num_preguntas_aleatorias", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_id_tema", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_id_docente", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_id_grupo", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_pct_facil",       Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_pct_media",       Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_pct_dificil",     Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_modo",            String.class,  ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_mensaje", String.class, ParameterMode.OUT);
        storedProcedure.registerStoredProcedureParameter("v_error", String.class, ParameterMode.OUT);


        storedProcedure.setParameter("v_tiempo_max", examenDTO.tiempo_maximo());
        storedProcedure.setParameter("v_numero_preguntas", examenDTO.numero_preguntas());
        storedProcedure.setParameter("v_porcentaje_curso", examenDTO.porcentaje_curso());
        storedProcedure.setParameter("v_nombre", examenDTO.nombre());
        storedProcedure.setParameter("v_descripcion", examenDTO.descripcion());
        storedProcedure.setParameter("v_porcentaje_aprobatorio", examenDTO.porcentaje_aprobatorio());

        storedProcedure.setParameter("p_fecha_inicio_str", examenDTO.fecha_hora_inicio());
        storedProcedure.setParameter("p_fecha_fin_str", examenDTO.fecha_hora_limite());

        storedProcedure.setParameter("v_num_preguntas_aleatorias", examenDTO.numero_preguntas_aleatorias());
        storedProcedure.setParameter("v_id_tema", examenDTO.tema_id());
        storedProcedure.setParameter("v_id_docente", examenDTO.docente_id());
        storedProcedure.setParameter("v_id_grupo", examenDTO.grupo_id());
        storedProcedure.setParameter("v_pct_facil", examenDTO.pct_facil());
        storedProcedure.setParameter("v_pct_media", examenDTO.pct_media());
        storedProcedure.setParameter("v_pct_dificil", examenDTO.pct_dificil());
        storedProcedure.setParameter("v_modo", examenDTO.modoAsignacion());

        storedProcedure.execute();

        // Leer mensaje de salida
        String mensaje = (String) storedProcedure.getOutputParameterValue("v_mensaje");
        String error   = (String) storedProcedure.getOutputParameterValue("v_error");
        if (error != null && !error.isBlank()) {
            throw new RuntimeException("Error al crear examen: " + error);
        }
        return mensaje;

    }

    @Override
    @Transactional
    public CrearPreguntaResponseDTO crearPregunta(String enunciado, Character esPublica, String tipoPregunta, Integer idTema, Integer idDocente) {
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("crear_pregunta");

        // Parámetros IN
        storedProcedure.registerStoredProcedureParameter("v_enunciado", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_es_publica", Character.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_tipo_pregunta", String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_id_tema", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_id_docente", Integer.class, ParameterMode.IN);

        // Parámetros OUT
        storedProcedure.registerStoredProcedureParameter("v_id_pregunta_creada", Integer.class, ParameterMode.OUT); // Nuevo parámetro OUT
        storedProcedure.registerStoredProcedureParameter("v_mensaje", String.class, ParameterMode.OUT);

        // Establecer valores de parámetros IN
        storedProcedure.setParameter("v_enunciado", enunciado);
        storedProcedure.setParameter("v_es_publica", esPublica);
        storedProcedure.setParameter("v_tipo_pregunta", tipoPregunta);
        storedProcedure.setParameter("v_id_tema", idTema);
        storedProcedure.setParameter("v_id_docente", idDocente);

        storedProcedure.execute();

        Integer idPreguntaCreada = (Integer) storedProcedure.getOutputParameterValue("v_id_pregunta_creada");
        String mensaje = (String) storedProcedure.getOutputParameterValue("v_mensaje");

        // Si el mensaje indica un error desde PL/SQL (porque no se lanzó una excepción SQL),
        // idPreguntaCreada podría ser null.
        if (mensaje != null && mensaje.toLowerCase().startsWith("error desde pl/sql")) {
            // Podrías lanzar una excepción aquí o simplemente devolver el DTO con idPreguntaCreada como null
            // throw new RuntimeException(mensaje);
        }


        return new CrearPreguntaResponseDTO(idPreguntaCreada, mensaje);
    }

    @Override
    @Transactional
    public String calificarExamen(Integer idPresentacionExamen) {
        // Crear una consulta para el procedimiento almacenado
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("calificar_examen");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("v_id_presentacion_examen", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("v_mensaje", String.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("v_id_presentacion_examen", idPresentacionExamen);

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        // Obtener el valor del parámetro de salida
        String mensaje = (String) storedProcedure.getOutputParameterValue("v_mensaje");

        // Retornar el mensaje
        return mensaje;
    }

    @Override
    @Transactional
    public List<PreguntaBancoDTO> obtenerPreguntasDocente(Integer idDocente) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("obtener_preguntas_docente")
                .declareParameters(
                        new SqlParameter("v_id_docente", OracleTypes.NUMBER),
                        new SqlOutParameter("p_preguntas", OracleTypes.CURSOR)
                )
                .returningResultSet("p_preguntas", this::mapRowToPreguntaBancoDTO);

        Map<String, Object> result = jdbcCall.execute(new MapSqlParameterSource("v_id_docente", idDocente));

        return (List<PreguntaBancoDTO>) result.get("p_preguntas");
    }

    @Override
    @Transactional
    public List<ExamenDTO> obtenerExamenesDocente(Integer id_docente) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("obtener_examenes_docente")
                .declareParameters(
                        new SqlParameter("v_id_docente", OracleTypes.NUMBER),
                        new SqlOutParameter("p_examenes", OracleTypes.CURSOR)
                )
                .returningResultSet("p_examenes", this::mapRowToExamenDTO);

        Map<String, Object> result = jdbcCall.execute(new MapSqlParameterSource("v_id_docente", id_docente));

        return (List<ExamenDTO>) result.get("p_examenes");
    }

    private ExamenDTO mapRowToExamenDTO(ResultSet rs, int rowNum) throws SQLException {
        return new ExamenDTO(
                rs.getInt("id_examen"),
                rs.getInt("tiempo_max"),
                rs.getInt("numero_preguntas"),
                rs.getFloat("porcentaje_curso"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getInt("porcentaje_aprobatorio"),
                rs.getDate("fecha_hora_inicio"),
                rs.getDate("fecha_hora_fin"),
                rs.getInt("num_preguntas_aleatorias"),
                rs.getInt("id_tema"),
                rs.getInt("id_docente"),
                rs.getInt("id_grupo"),
                rs.getString("estado")
        );
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
        storedProcedure.setParameter("rol", rol);

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        String nombre = (String) storedProcedure.getOutputParameterValue("res");

        return nombre;

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
        storedProcedure.setParameter("rol", rol);

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        String json1 = (String) storedProcedure.getOutputParameterValue("res");

        System.out.println("JSON from DB for get_cursos_por_usuario (ID " + id + ", Rol " + rol + "): |" + json1 + "|");
        if (json1 == null || json1.trim().isEmpty() || json1.equalsIgnoreCase("null")) { // Handle "null" string too
            System.out.println("No JSON data returned from DB, returning empty list.");
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        Type personListType = new TypeToken<List<CursoSimpleDTO>>() {}.getType();

        try {
            return gson.fromJson(json1, personListType);
        } catch (JsonSyntaxException e) {
            System.err.println("RAW JSON causing error: |" + json1 + "|"); // Log it again on error
            System.err.println("Error parsing JSON with Gson: ");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    @Override
    public List<TemasCursoDTO> obtenerTemasCurso(Integer id_grupo) {
        // Crear una consulta para el procedimiento almacenado
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("get_temas_por_curso");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("p_id_grupo", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("res", String.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("p_id_grupo", id_grupo);

        // Ejecutar el procedimiento almacenado
        storedProcedure.execute();

        String json1 = (String) storedProcedure.getOutputParameterValue("res");
        Gson gson = new Gson();
        Type personListType = new TypeToken<List<TemasCursoDTO>>() {}.getType();

        return gson.fromJson(json1, personListType);
    }


    @Override
    public List<TemasCursoDTO> obtenerTemasDocente() {
        List<Object[]> resultados = examenRepository.obtenerCursos();
        return resultados.stream()
                .map(fila -> new TemasCursoDTO(""+fila[0], (String) fila[1]))
                .collect(Collectors.toList());
    }


    //Esta vaina es solo para permitir consultar las preguntas por tema, es como un plugin XD
    private PreguntaBancoDTO mapRowToPreguntaBancoDTO(ResultSet rs, int rowNum) throws SQLException {
        return new PreguntaBancoDTO(
                rs.getInt("id_pregunta"),
                rs.getString("enunciado"),
                rs.getString("es_publica").charAt(0),
                rs.getString("tipo_pregunta"),
                rs.getInt("id_tema"),
                rs.getInt("id_docente")
        );
    }

    @Override
    public List<GrupoSimpleDTO> obtenerGruposPorCurso(Integer idCurso, Integer idDocente) {
        // Crear una consulta para el procedimiento almacenado
        StoredProcedureQuery storedProcedure = entityManager.createStoredProcedureQuery("get_grupos_por_curso");

        // Registrar los parámetros de entrada y salida del procedimiento almacenado
        storedProcedure.registerStoredProcedureParameter("p_id_curso", Integer.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter("p_id_docente", Integer.class, ParameterMode.IN); // Nuevo parámetro
        storedProcedure.registerStoredProcedureParameter("res", String.class, ParameterMode.OUT);

        // Establecer los valores de los parámetros de entrada
        storedProcedure.setParameter("p_id_curso", idCurso);
        storedProcedure.setParameter("p_id_docente", idDocente); // Establecer el nuevo parámetro

        try {
            // Ejecutar el procedimiento almacenado
            storedProcedure.execute();

            // Obtener el resultado (JSON como String)
            String jsonResult = (String) storedProcedure.getOutputParameterValue("res");

            // Verificar si el resultado es nulo o indicativo de error desde PL/SQL
            if (jsonResult == null || jsonResult.trim().isEmpty() || jsonResult.toLowerCase().contains("error")) {
                System.err.println("Error desde PL/SQL o resultado vacío para get_grupos_por_curso: " + jsonResult);
                return Collections.emptyList();
            }

            // Deserializar el JSON a una lista de GrupoSimpleDTO
            // Si GrupoSimpleDTO no tiene campos LocalDate, no necesitas el LocalDateAdapter aquí.
            // Si otros DTOs que Gson pudiera inferir indirectamente lo necesitaran, entonces sí.
            // Por seguridad, si tienes LocalDate en cualquier DTO que Gson pueda tocar, es mejor registrar el adapter.
            // Para este caso específico, asumiendo que GrupoSimpleDTO es simple:
            Gson gson = new Gson();
            // Si necesitaras LocalDateAdapter para otros DTOs que Gson pudiera encontrar:
            // Gson gson = new GsonBuilder()
            //                 .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            //                 .create();


            Type grupoSimpleListType = new TypeToken<List<GrupoSimpleDTO>>() {}.getType();

            return gson.fromJson(jsonResult, grupoSimpleListType);

        } catch (Exception e) {
            e.printStackTrace(); // Considera un logging más robusto
            // Podrías lanzar una excepción personalizada aquí para que el controlador la maneje
            // throw new RuntimeException("Error al procesar la obtención de grupos del curso", e);
            return Collections.emptyList();
        }
    }
}
