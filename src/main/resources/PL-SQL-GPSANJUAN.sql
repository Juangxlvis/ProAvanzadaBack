
CREATE OR REPLACE PROCEDURE login(p_id IN VARCHAR2, rol IN VARCHAR2, res OUT CHAR) AS
    cursor c_alumno is select *
                       from alumno a
                       where a.ID_ALUMNO = p_id;
    cursor c_docente is select *
                        from docente d
                        where d.ID_DOCENTE = p_id;
    v_alumno  alumno%ROWTYPE;
    v_docente docente%ROWTYPE;
BEGIN
    if rol = 'docente' then

        open c_docente;
        fetch c_docente into v_docente;
        if c_docente%FOUND then
            res := '1';
            dbms_output.put_line('docente encontrado');
        else
            res := '0';
        end if;
        close c_docente;

    else

        open c_alumno;
        fetch c_alumno into v_alumno;
        if c_alumno%FOUND then
            res := '1';
            dbms_output.put_line('alumno encontrado');
        else
            res := '0';
        end if;
        close c_alumno;
    end if;
end;

-- Obtener el nombre dado el id del usuario y el rol.
create or replace procedure get_nombre_usuario(p_id_usuario in varchar2, rol in varchar2, res out varchar2) as
BEGIN
    if rol = 'docente' then
        SELECT d.NOMBRE || ' ' || d.APELLIDO
        INTO res
        from docente d
        where d.ID_DOCENTE = p_id_usuario;
    else
        SELECT a.NOMBRE || ' ' || a.APELLIDO
        INTO res
        from alumno a
        where a.ID_ALUMNO = p_id_usuario;
    end if;
end;


-- (HECHO) Procedimiento que retorna los grupos de un usuario dado su id y rol.
-- Retorna un JSON con los grupos del usuario, especficando el id del grupo, el nombre del grupo y el nombre del curso.
CREATE OR REPLACE PROCEDURE get_grupos_por_usuario(
    p_id_usuario IN VARCHAR2,
    rol_in IN VARCHAR2,
    res OUT CLOB
) AS
    v_json_result CLOB;
BEGIN
    IF LOWER(rol_in) = 'docente' THEN
        SELECT COALESCE(
                       JSON_ARRAYAGG(
                               JSON_OBJECT(
                                       'id_grupo'     VALUE id_grupo,
                                       'nombre_grupo' VALUE nombre_grupo,
                                   -- Apply workaround for nombre_curso
                                       'nombre_curso' VALUE '"' || REPLACE(nombre_curso, '"', '\"') || '"'
                                       FORMAT JSON -- Keep FORMAT JSON, it might help with other fields
                               )
                       ),
                       JSON_ARRAY()
               )
        INTO v_json_result
        FROM (
                 SELECT g.ID_GRUPO id_grupo,
                        g.NOMBRE   nombre_grupo,
                        c.NOMBRE   nombre_curso
                 FROM docente d
                          JOIN grupo g ON (d.ID_DOCENTE = g.id_docente)
                          JOIN curso c ON (c.id_curso = g.id_curso)
                 WHERE d.ID_DOCENTE = p_id_usuario
             );
    ELSIF LOWER(rol_in) = 'alumno' THEN
        SELECT COALESCE(
                       JSON_ARRAYAGG(
                               JSON_OBJECT(
                                       'id_grupo'     VALUE id_grupo,
                                       'nombre_grupo' VALUE nombre_grupo,
                                   -- Apply workaround for nombre_curso
                                       'nombre_curso' VALUE '"' || REPLACE(nombre_curso, '"', '\"') || '"'
                                       FORMAT JSON
                               )
                       ),
                       JSON_ARRAY()
               )
        INTO v_json_result
        FROM (
                 SELECT ag.ID_GRUPO id_grupo, g.NOMBRE nombre_grupo, c.NOMBRE nombre_curso
                 FROM alumno_grupo ag
                          JOIN grupo g ON (ag.id_grupo = g.id_grupo)
                          JOIN curso c ON (c.id_curso = g.id_curso)
                 WHERE ag.id_alumno = p_id_usuario
             );
    ELSE
        v_json_result := JSON_ARRAY();
    END IF;

    res := v_json_result;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error in get_grupos_por_usuario: ' || SQLERRM);
        -- Return a valid JSON error object
        res := JSON_OBJECT('error' VALUE ('Error en el procedimiento almacenado: ' || SQLERRM) FORMAT JSON);
END get_grupos_por_usuario;

CREATE OR REPLACE PROCEDURE get_grupos_por_curso (
    p_id_curso   IN NUMBER,
    p_id_docente IN NUMBER, -- Parámetro para el ID del docente
    res          OUT CLOB
) AS
BEGIN
    -- Seleccionar y agregar los grupos como un array JSON
SELECT
    JSON_ARRAYAGG( -- Agrega los JSON_OBJECT resultantes en un array JSON
            JSON_OBJECT( -- Crea un objeto JSON para cada fila del grupo
                    'id_grupo'     VALUE g.id_grupo, -- Solo ID del grupo
                    'nombre_grupo' VALUE g.nombre    -- Solo nombre del grupo
            ) RETURNING CLOB -- Asegura que cada JSON_OBJECT se trate como CLOB
    )
INTO res -- Almacena el resultado directamente en el parámetro de salida
FROM
    Grupo g -- Nombre de tu tabla de grupos (asegúrate que sea el correcto)
WHERE
    g.id_curso   = p_id_curso AND -- Condición para filtrar por curso
    g.id_docente = p_id_docente;  -- Condición para filtrar por docente

-- JSON_ARRAYAGG devuelve '[]' (un array JSON vacío, no SQL NULL) si no hay filas.
-- Esta comprobación es una salvaguarda por si 'res' quedara NULL en algún caso extremo.
IF res IS NULL THEN
        res := TO_CLOB('[]'); -- Asigna un array JSON vacío como CLOB
END IF;

EXCEPTION
    WHEN OTHERS THEN
        DECLARE
l_error_message VARCHAR2(3000);
            l_json_error_string VARCHAR2(3200);
            l_sqlerrm_text VARCHAR2(2800);
BEGIN
            -- Escapar caracteres especiales en SQLERRM para que sea un JSON válido
            l_sqlerrm_text := SUBSTR(SQLERRM, 1, 2800);
            l_sqlerrm_text := REPLACE(l_sqlerrm_text, '\', '\\');
            l_sqlerrm_text := REPLACE(l_sqlerrm_text, '"', '\"');
            l_sqlerrm_text := REPLACE(l_sqlerrm_text, chr(10), '\n');
            l_sqlerrm_text := REPLACE(l_sqlerrm_text, chr(13), '\r');
            l_sqlerrm_text := REPLACE(l_sqlerrm_text, chr(9), '\t');

            l_error_message := 'Error en el procedimiento almacenado: ' || l_sqlerrm_text;

            DBMS_OUTPUT.PUT_LINE('Error en get_grupos_por_curso (raw SQLERRM): ' || SUBSTR(SQLERRM, 1, 500));

            l_json_error_string := '{"error":"' || l_error_message || '"}';

BEGIN
                res := TO_CLOB(l_json_error_string);
EXCEPTION
                WHEN VALUE_ERROR THEN
                    res := TO_CLOB('{"error":"Mensaje de error demasiado largo o inválido para conversión JSON."}');
WHEN OTHERS THEN
                    res := TO_CLOB('{"error":"Fallback: Error inesperado creando JSON de error."}');
END;
END;
END get_grupos_por_curso;
/

-- obtenerExamenesPresentadosAlumnoGrupo()
-- @descripción: Se encarga de obtener la presentacion_examen de un alumno específico en un grupo específico
-- @return: retorna un cursor con todos las presentación_examen que cumplan
-- DTO-in: id-alumno, id_grupo
-- DTO-out: Lista de - > (Presentacion_examen)


CREATE OR REPLACE PROCEDURE tomar_examenes_alumno_grupo(
    v_id_alumno IN alumno.id_alumno%TYPE,
    v_id_grupo IN grupo.id_grupo%TYPE,
    p_examenes OUT SYS_REFCURSOR
)
    IS
BEGIN
    OPEN p_examenes FOR
        SELECT pe.*
        FROM presentacion_examen pe
                 JOIN examen e ON pe.id_examen = e.id_examen
                 JOIN Grupo g ON e.id_grupo = g.id_grupo
        WHERE pe.id_alumno = v_id_alumno
          AND g.id_grupo = v_id_grupo;
END tomar_examenes_alumno_grupo;
/

(HECHO)
CREATE OR REPLACE PROCEDURE GET_PRESENTACION_EXAMEN_ALUMNO_GRUPO(
    p_id_alumno IN NUMBER,
    p_id_grupo  IN NUMBER,
    res         OUT CLOB
)
AS
BEGIN
  SELECT JSON_ARRAYAGG(
           JSON_OBJECT(
             'idExamen'      VALUE pe.id_presentacion_examen,
             'nombreExamen'  VALUE e.nombre,
             'calificacion'  VALUE TO_CHAR(pe.calificacion)
             FORMAT JSON
           )
         )
    INTO res
    FROM presentacion_examen pe
    JOIN examen e   ON pe.id_examen = e.id_examen
    JOIN grupo g    ON e.id_grupo   = g.id_grupo
   WHERE pe.terminado    = '1'
     AND pe.calificacion IS NOT NULL
     AND pe.id_alumno    = p_id_alumno
     AND g.id_grupo      = p_id_grupo;
END GET_PRESENTACION_EXAMEN_ALUMNO_GRUPO;
/


(HECHO)
-- Se compara cuales son los examenes que un alumno tiene pendientes por presentar en un grupo específico.
-- Esto se compara observando cuales son los examenes existentes y restando los examenes que ya ha presentado el alumno.
create or replace procedure get_examenes_grupo_pendientes_por_alumno(p_id_alumno in number, p_id_grupo in number, res out clob) as
    v_json CLOB;
BEGIN
    SELECT JSON_ARRAYAGG(
                   JSON_OBJECT(
                           'id_examen' VALUE id_examen,
                           'tiempo_max' VALUE TIEMPO_MAX,
                           'numero_preguntas' VALUE NUMERO_PREGUNTAS,
                           'porcentaje_aprobatorio' VALUE PORCENTAJE_APROBATORIO,
                           'nombre' VALUE nombre,
                           'porcentaje_curso' VALUE PORCENTAJE_CURSO,
                           'fecha_hora_inicio' VALUE TO_CHAR(fecha_hora_inicio, 'DD/MM/YYYY'),
                           'fecha_hora_fin' VALUE TO_CHAR(fecha_hora_fin, 'HH24:MI'),
                           'tema' VALUE '"' || titulo || '"'
                           FORMAT JSON
                   )
           )
    INTO v_json
    FROM (SELECT e.*, t.titulo as titulo
          FROM examen e
                   join tema t on (e.id_tema = t.id_tema)
                   join GRUPO g on (g.ID_GRUPO = p_id_grupo AND e.id_grupo = g.id_grupo)
          WHERE e.id_examen NOT IN (SELECT pe.id_examen
                                    FROM presentacion_examen pe
                                    WHERE pe.id_alumno = p_id_alumno));
    res := v_json;
END get_examenes_grupo_pendientes_por_alumno;

-- 2. crear_presentacion_examen
-- Inserta una nueva fila en presentacion_examen con todos los campos que tu servicio espera.
CREATE OR REPLACE PROCEDURE crear_presentacion_examen(
    p_tiempo                  IN NUMBER,
    p_terminado               IN CHAR,
    p_ip                      IN VARCHAR2,
    p_fecha_hora_presentacion IN DATE,
    p_id_examen               IN NUMBER,
    p_id_alumno               IN NUMBER,
    p_mensaje                 OUT VARCHAR2
) AS
BEGIN
    INSERT INTO presentacion_examen (
        tiempo,
        terminado,
        ip_source,
        fecha_hora_presentacion,
        id_examen,
        id_alumno
    ) VALUES (
        p_tiempo,
        p_terminado,
        p_ip,
        p_fecha_hora_presentacion,
        p_id_examen,
        p_id_alumno
    );
    p_mensaje := 'Presentación creada exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        p_mensaje := 'Error al crear presentación: ' || SQLERRM;
END crear_presentacion_examen;
/


CREATE OR REPLACE PROCEDURE crear_presentacion_examen(
    v_id_examen IN presentacion_examen.id_examen%TYPE,
    v_id_alumno IN presentacion_examen.id_alumno%TYPE,
    v_mensaje OUT VARCHAR2 -- Mover al final de la lista de parámetros y utilizar OUT
)
    IS

BEGIN
    INSERT INTO presentacion_examen (tiempo, terminado, calificacion, ip_source, fecha_hora_presentacion, id_examen, id_alumno)
    Values (null, '0', 0, '192.168.0.1', sysdate, v_id_examen, v_id_alumno);

    v_mensaje := 'presentación_examen se ha creado exitosamente';

EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al crear la presentacion_examen:   ' || SQLERRM;

END crear_presentacion_examen;
/


-- este procedimiento se encarga de calificar el examen una vez presentado
CREATE OR REPLACE PROCEDURE calificar_examen (
    v_id_presentacion_examen IN presentacion_examen.id_presentacion_examen%TYPE,
    v_calificacion IN presentacion_examen.calificacion%TYPE,
    v_mensaje OUT VARCHAR2
) IS
BEGIN
    UPDATE presentacion_examen
    SET calificacion = v_calificacion
    WHERE id_presentacion_examen = v_id_presentacion_examen;

    v_mensaje := 'Examen calificado exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al calificar el examen: ' || SQLERRM;
END calificar_examen;
/


CREATE OR REPLACE PROCEDURE  calificar_pregunta (
    v_id_presentacion_pregunta IN presentacion_pregunta.id_presentacion_pregunta%TYPE,
    v_respuesta_correcta IN presentacion_pregunta.respuesta_correcta%TYPE,
    v_mensaje out varchar2
) IS
BEGIN
    UPDATE presentacion_pregunta
    SET respuesta_correcta = v_respuesta_correcta
    WHERE id_presentacion_pregunta = v_id_presentacion_pregunta;

    v_mensaje := 'Pregunta calificada exitosamente';

END calificar_pregunta;
/



CREATE OR REPLACE PROCEDURE crear_pregunta (
    v_enunciado           IN pregunta.enunciado%TYPE,
    v_es_publica          IN pregunta.es_publica%TYPE,
    v_tipo_pregunta       IN pregunta.tipo_pregunta%TYPE,
    v_id_tema             IN pregunta.id_tema%TYPE,
    v_id_docente          IN pregunta.id_docente%TYPE,
    v_id_pregunta_creada  OUT pregunta.id_pregunta%TYPE, -- Nuevo parámetro OUT para el ID
    v_mensaje             OUT VARCHAR2
)
IS
BEGIN
    -- Inicializar parámetros de salida
    v_id_pregunta_creada := NULL;
    v_mensaje := NULL;

    -- Validación de parámetros NOT NULL (igual que antes)
    IF v_enunciado IS NULL THEN
        v_mensaje := 'Error desde PL/SQL: El valor para "enunciado" no puede ser nulo.';
        DBMS_OUTPUT.PUT_LINE(v_mensaje);
        RETURN;
END IF;
    IF v_es_publica IS NULL THEN
        v_mensaje := 'Error desde PL/SQL: El valor para "es_publica" no puede ser nulo.';
        DBMS_OUTPUT.PUT_LINE(v_mensaje);
        RETURN;
END IF;
    IF v_tipo_pregunta IS NULL THEN
        v_mensaje := 'Error desde PL/SQL: El valor para "tipo_pregunta" no puede ser nulo.';
        DBMS_OUTPUT.PUT_LINE(v_mensaje);
        RETURN;
END IF;
    IF v_id_tema IS NULL THEN
        v_mensaje := 'Error desde PL/SQL: El valor para "id_tema" no puede ser nulo.';
        DBMS_OUTPUT.PUT_LINE(v_mensaje);
        RETURN;
END IF;
    IF v_id_docente IS NULL THEN
        v_mensaje := 'Error desde PL/SQL: El valor para "id_docente" no puede ser nulo.';
        DBMS_OUTPUT.PUT_LINE(v_mensaje);
        RETURN;
END IF;

INSERT INTO pregunta (
    ID_PREGUNTA,
    enunciado,
    es_publica,
    tipo_pregunta,
    id_tema,
    id_docente,
    estado
) VALUES (
             PREGUNTA_SEQ.NEXTVAL,
             v_enunciado,
             v_es_publica,
             v_tipo_pregunta,
             v_id_tema,
             v_id_docente,
             'ACTIVA'
         )
    RETURNING ID_PREGUNTA INTO v_id_pregunta_creada; -- Captura el ID generado

v_mensaje := 'Pregunta creada exitosamente. ID: ' || v_id_pregunta_creada;

EXCEPTION
    WHEN OTHERS THEN
        v_id_pregunta_creada := NULL; -- Asegurarse que el ID sea nulo en caso de error
        DBMS_OUTPUT.PUT_LINE('Error en crear_pregunta (EXCEPTION): ' || SQLCODE || ' - ' || SQLERRM);
        v_mensaje := 'Error al crear la pregunta: ' || SQLERRM;
END crear_pregunta;
/



-- Obtener preguntas por examen
create or replace procedure get_preguntas_por_examen (p_id_examen number, res clob) is
begin
    select JSON_ARRAYAGG(
        JSON_OBJECT(
            'id_pregunta' VALUE id_pregunta,
            'enunciado' VALUE '"' || enunciado || '"',
            'tipo_pregunta' VALUE tipo_pregunta,
            'id_tema' VALUE id_tema,
            'id_docente' VALUE id_docente
            FORMAT JSON
        )
    )
    into res
    from pregunta p
    join PREGUNTA_EXAMEN pe on p.id_pregunta = pe.id_pregunta
    where pe.id_examen = p_id_examen;
end get_preguntas_por_examen;
/


create or replace procedure get_temas_por_curso (p_id_grupo IN number, res out clob) IS
BEGIN
    SELECT JSON_ARRAYAGG(
                   JSON_OBJECT(
                           'id_tema' VALUE id_tema,
                           'titulo' VALUE '"' || titulo || '"'
                           FORMAT JSON
                   )
           )
    INTO res
    FROM (select t.ID_TEMA id_tema, t.TITULO titulo from tema t join unidad u on t.UNIDAD_ID_UNIDAD = u.ID_UNIDAD
    join curso c on  u.ID_CURSO = c.ID_CURSO
    join grupo g on c.ID_CURSO = g.ID_CURSO
    where g.ID_GRUPO = p_id_grupo);

END get_temas_por_curso;
/


-- 1. Obtener banco de preguntas por tema (públicas y privadas)
CREATE OR REPLACE PROCEDURE get_banco_preguntas(
    p_id_tema  IN NUMBER,
    res        OUT CLOB
) AS
BEGIN
    SELECT JSON_ARRAYAGG(
               JSON_OBJECT(
                   'id_pregunta' VALUE p.id_pregunta,
                   'enunciado'    VALUE p.enunciado,
                   'es_publica'   VALUE p.es_publica,
                   'tipo'         VALUE p.tipo_pregunta
                   FORMAT JSON
               )
           )
    INTO res
    FROM pregunta p
    WHERE p.id_tema = p_id_tema
      AND p.estado = 'ACTIVA';
END get_banco_preguntas;
/


-- 2. CRUD de opciones de respuesta
create or replace PROCEDURE crear_respuesta(
    v_descripcion   IN RESPUESTA.DESCRIPCION%TYPE,
    v_es_verdadera  IN RESPUESTA.ES_VERDADERA%TYPE,
    v_id_pregunta   IN RESPUESTA.ID_PREGUNTA%TYPE, -- Parámetro para ID_PREGUNTA
    v_mensaje       OUT VARCHAR2
) AS
BEGIN
INSERT INTO RESPUESTA (
    ID_RESPUESTA,
    DESCRIPCION,
    ES_VERDADERA,
    ID_PREGUNTA -- Se incluye en la lista de columnas
)
VALUES (
           RESPUESTA_SEQ.NEXTVAL,
           v_descripcion,
           v_es_verdadera,
           v_id_pregunta -- Se usa el parámetro v_id_pregunta
       );
v_mensaje := 'Respuesta creada exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al crear respuesta: ' || SQLERRM;
END crear_respuesta;


CREATE OR REPLACE PROCEDURE actualizar_respuesta(
    v_id_respuesta  IN RESPUESTA.ID_RESPUESTA%TYPE,
    v_descripcion   IN RESPUESTA.DESCRIPCION%TYPE,
    v_es_verdadera  IN RESPUESTA.ES_VERDADERA%TYPE,
    v_mensaje       OUT VARCHAR2
) AS
BEGIN
    UPDATE RESPUESTA
      SET DESCRIPCION  = v_descripcion,
          ES_VERDADERA = v_es_verdadera
    WHERE ID_RESPUESTA = v_id_respuesta;
    v_mensaje := 'Respuesta actualizada exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al actualizar respuesta: ' || SQLERRM;
END actualizar_respuesta;
/

CREATE OR REPLACE PROCEDURE borrar_respuesta(
    v_id_respuesta  IN RESPUESTA.ID_RESPUESTA%TYPE,
    v_mensaje       OUT VARCHAR2
) AS
BEGIN
    DELETE FROM RESPUESTA
    WHERE ID_RESPUESTA = v_id_respuesta;
    v_mensaje := 'Respuesta eliminada exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al eliminar respuesta: ' || SQLERRM;
END borrar_respuesta;
/

-- 3. Obtener unidades y temas por curso
CREATE OR REPLACE PROCEDURE get_unidades_por_curso(
    p_id_curso IN NUMBER,
    res        OUT CLOB
) AS
BEGIN
    SELECT JSON_ARRAYAGG(
               JSON_OBJECT(
                   'id_unidad'   VALUE u.id_unidad,
                   'titulo'      VALUE u.titulo,
                   'descripcion' VALUE u.descripcion,
                   'temas'       VALUE (
                       SELECT JSON_ARRAYAGG(
                                  JSON_OBJECT(
                                    'id_tema' VALUE t.id_tema,
                                    'nombre'  VALUE t.titulo
                                  ) FORMAT JSON
                              )
                       FROM tema t
                       WHERE t.unidad_id_unidad = u.id_unidad
                   ) FORMAT JSON
               ) FORMAT JSON
           )
      INTO res
      FROM unidad u
     WHERE u.id_curso = p_id_curso;
END get_unidades_por_curso;
/

--4 obtener el horario de un grupo:
CREATE OR REPLACE PROCEDURE get_horario_grupo(
    p_id_grupo IN NUMBER,
    res        OUT CLOB
) AS
BEGIN
    SELECT JSON_ARRAYAGG(
               JSON_OBJECT(
                   'dia'         VALUE bh.dia,
                   'hora_inicio' VALUE TO_CHAR(bh.hora_inicio, 'HH24:MI'),
                   'hora_fin'    VALUE TO_CHAR(bh.hora_fin, 'HH24:MI'),
                   'lugar'       VALUE bh.lugar
                   FORMAT JSON
               )
           )
    INTO res
    FROM horario h
    JOIN bloque_horario bh ON h.id_bloque_horario = bh.id_bloque_horario
    WHERE h.id_grupo = p_id_grupo;
END get_horario_grupo;
/


CREATE OR REPLACE PROCEDURE crear_examen(
    -- Parámetros básicos
    p_tiempo_max               IN examen.tiempo_max%TYPE,
    p_numero_preguntas         IN examen.numero_preguntas%TYPE,
    p_porcentaje_curso         IN examen.porcentaje_curso%TYPE,
    p_nombre                   IN examen.nombre%TYPE,
    p_descripcion              IN examen.descripcion%TYPE,
    p_porcentaje_aprobatorio   IN examen.porcentaje_aprobatorio%TYPE,
    p_fecha_inicio             IN examen.fecha_hora_inicio%TYPE,
    p_fecha_fin                IN examen.fecha_hora_fin%TYPE,
    p_num_preguntas_aleatorias IN examen.num_preguntas_aleatorias%TYPE,
    p_id_tema                  IN examen.id_tema%TYPE,
    p_id_docente               IN examen.id_docente%TYPE,
    p_id_grupo                 IN examen.id_grupo%TYPE,
    -- Parámetros de balanceo
    p_pct_facil                IN NUMBER,
    p_pct_media                IN NUMBER,
    p_pct_dificil              IN NUMBER,
    p_modo                     IN VARCHAR2,
    -- Mensaje de salida
    p_mensaje                  OUT VARCHAR2
) AS
    v_new_id           examen.id_examen%TYPE;
    v_total_insertadas INTEGER;
    v_sin_porcentaje   INTEGER;
    v_count            INTEGER;
BEGIN
    ----------------------------------------------------------
    -- 1) Validar fechas
    ----------------------------------------------------------
    IF p_fecha_inicio < TRUNC(SYSDATE) OR p_fecha_fin < p_fecha_inicio THEN
        RAISE_APPLICATION_ERROR(-20001, 'Fechas inválidas: inicio debe ser ≥ hoy y fin ≥ inicio');
END IF;

    ----------------------------------------------------------
    -- 2) Validar que el docente pertenece al grupo
    ----------------------------------------------------------
SELECT COUNT(*) INTO v_count
FROM grupo
WHERE id_grupo   = p_id_grupo
  AND id_docente = p_id_docente;
IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20002,
            'El docente no está asignado al grupo '||p_id_grupo);
END IF;

    ----------------------------------------------------------
    -- 3) Insertar examen y obtener su ID
    ----------------------------------------------------------
INSERT INTO examen(
    id_examen,
    tiempo_max, numero_preguntas, porcentaje_curso,
    nombre, descripcion, porcentaje_aprobatorio,
    fecha_hora_inicio, fecha_hora_fin, num_preguntas_aleatorias,
    id_tema, id_docente, id_grupo, estado
) VALUES (
             examen_seq.NEXTVAL,
             p_tiempo_max, p_numero_preguntas, p_porcentaje_curso,
             p_nombre, p_descripcion, p_porcentaje_aprobatorio,
             p_fecha_inicio, p_fecha_fin, p_num_preguntas_aleatorias,
             p_id_tema, p_id_docente, p_id_grupo, 'Activa'
         )
    RETURNING id_examen INTO v_new_id;

-- 4) Asignar preguntas 'AUTO' o 'MIXTO'

IF UPPER(p_modo) IN ('AUTO','MIXTO') THEN
        -- Si es MIXTO, validar que no exceda 50% difíciles
        IF UPPER(p_modo) = 'MIXTO' THEN
            IF p_pct_dificil > 50 THEN
                RAISE_APPLICATION_ERROR(-20003,
                    'Modo MIXTO no permite más de 50% preguntas difíciles');
END IF;
END IF;
        -- Llamada a la rutina de balanceo
        asignar_preguntas(
            p_id_examen   => v_new_id,
            p_total       => p_numero_preguntas,
            p_pct_facil   => p_pct_facil,
            p_pct_media   => p_pct_media,
            p_pct_dificil => p_pct_dificil
        );
END IF;


CREATE OR REPLACE PROCEDURE crear_examen(
    -- Parámetros básicos (IN)
    p_tiempo_max               IN examen.tiempo_max%TYPE,
    p_numero_preguntas         IN examen.numero_preguntas%TYPE,
    p_porcentaje_curso         IN examen.porcentaje_curso%TYPE,
    p_nombre                   IN examen.nombre%TYPE,
    p_descripcion              IN examen.descripcion%TYPE,
    p_porcentaje_aprobatorio   IN examen.porcentaje_aprobatorio%TYPE,
    p_fecha_inicio_str         IN VARCHAR2,
    p_fecha_fin_str            IN VARCHAR2,
    p_num_preguntas_aleatorias IN examen.num_preguntas_aleatorias%TYPE,
    p_id_tema                  IN examen.id_tema%TYPE,
    p_id_docente               IN examen.id_docente%TYPE,
    p_id_grupo                 IN examen.id_grupo%TYPE,
    -- Parámetros de balanceo (IN)
    p_pct_facil                IN NUMBER,
    p_pct_media                IN NUMBER,
    p_pct_dificil              IN NUMBER,
    p_modo                     IN VARCHAR2, -- Ahora espera 'AUTO', 'MIXTO', o 'MANUAL'
    -- Mensajes de salida (OUT)
    p_mensaje                  OUT VARCHAR2,
    p_error                    OUT VARCHAR2
) AS
    v_new_id                   examen.id_examen%TYPE;
    v_total_insertadas         INTEGER;
    v_sin_porcentaje           INTEGER;
    v_count                    INTEGER;
    v_fecha_inicio_dt          DATE;
    v_fecha_fin_dt             DATE;
    v_formato_fecha            CONSTANT VARCHAR2(30) := 'YYYY-MM-DD HH24:MI:SS';
BEGIN
    p_error := NULL;
    p_mensaje := NULL;

BEGIN
        v_fecha_inicio_dt := TO_DATE(p_fecha_inicio_str, v_formato_fecha);
        v_fecha_fin_dt    := TO_DATE(p_fecha_fin_str, v_formato_fecha);
EXCEPTION
        WHEN OTHERS THEN
            p_error := 'Error PL/SQL: Formato de fecha inválido para inicio o fin. Se esperaba ''' || v_formato_fecha || '''. Recibido inicio: [' || p_fecha_inicio_str || '], fin: [' || p_fecha_fin_str || ']. Detalle Oracle: ' || SQLERRM;
            DBMS_OUTPUT.PUT_LINE(p_error);
            RETURN;
END;

    IF v_fecha_inicio_dt < TRUNC(SYSDATE) OR v_fecha_fin_dt < v_fecha_inicio_dt THEN
        p_error := 'Error PL/SQL: Fechas inválidas. Inicio: ' || TO_CHAR(v_fecha_inicio_dt, v_formato_fecha) ||
                   ', Fin: ' || TO_CHAR(v_fecha_fin_dt, v_formato_fecha) ||
                   '. El inicio debe ser >= hoy y fin >= inicio.';
        DBMS_OUTPUT.PUT_LINE(p_error);
        RETURN;
END IF;

SELECT COUNT(*) INTO v_count
FROM grupo
WHERE id_grupo   = p_id_grupo
  AND id_docente = p_id_docente;

IF v_count = 0 THEN
        p_error := 'Error PL/SQL: El docente ' || p_id_docente || ' no está asignado al grupo ' || p_id_grupo;
        DBMS_OUTPUT.PUT_LINE(p_error);
        RETURN;
END IF;

INSERT INTO examen(
    id_examen,
    tiempo_max, numero_preguntas, porcentaje_curso,
    nombre, descripcion, porcentaje_aprobatorio,
    fecha_hora_inicio, fecha_hora_fin,
    num_preguntas_aleatorias,
    id_tema, id_docente, id_grupo, estado
) VALUES (
             examen_seq.NEXTVAL,
             p_tiempo_max, p_numero_preguntas, p_porcentaje_curso,
             p_nombre, p_descripcion, p_porcentaje_aprobatorio,
             v_fecha_inicio_dt, v_fecha_fin_dt,
             p_num_preguntas_aleatorias,
             p_id_tema, p_id_docente, p_id_grupo, 'ACTIVO'
         )
    RETURNING id_examen INTO v_new_id;

-- CORRECCIÓN AQUÍ: Reconocer 'AUTO' como modo automático
IF UPPER(p_modo) IN ('AUTO','MIXTO') THEN -- Cambiado 'A' de nuevo a 'AUTO'
        IF UPPER(p_modo) = 'MIXTO' THEN
            IF p_pct_dificil > 50 THEN
                 p_error := 'Error PL/SQL: Modo MIXTO no permite más de 50% preguntas difíciles.';
                 DBMS_OUTPUT.PUT_LINE(p_error);
                 RETURN;
END IF;
END IF;

        asignar_preguntas(
            p_id_examen   => v_new_id,
            p_total       => p_numero_preguntas,
            p_pct_facil   => p_pct_facil,
            p_pct_media   => p_pct_media,
            p_pct_dificil => p_pct_dificil
        );
END IF;

SELECT COUNT(*) INTO v_total_insertadas
FROM pregunta_examen
WHERE id_examen = v_new_id;

IF v_total_insertadas != p_numero_preguntas AND UPPER(p_modo) != 'MANUAL' THEN
        p_error := 'Error PL/SQL: Total preguntas asignadas ('||v_total_insertadas||') es diferente del esperado ('||p_numero_preguntas||') para el modo ' || p_modo;
        DBMS_OUTPUT.PUT_LINE(p_error);
        RETURN;
END IF;

SELECT COUNT(*) INTO v_sin_porcentaje
FROM pregunta_examen
WHERE id_examen = v_new_id
  AND porcentaje_examen IS NULL;

IF v_sin_porcentaje > 0 AND UPPER(p_modo) != 'MANUAL' THEN
        p_error := 'Error PL/SQL: Hay '||v_sin_porcentaje||' preguntas asignadas sin porcentaje definido para el modo ' || p_modo;
        DBMS_OUTPUT.PUT_LINE(p_error);
        RETURN;
END IF;

    p_mensaje := 'Examen '||v_new_id||' creado exitosamente en modo '||p_modo;

EXCEPTION
    WHEN OTHERS THEN
        IF p_error IS NULL THEN
            p_error   := 'Error PL/SQL (EXCEPTION): '||SUBSTR(SQLERRM, 1, 250);
END IF;
        p_mensaje := NULL;
        DBMS_OUTPUT.PUT_LINE('Excepción en crear_examen: ' || SQLCODE || ' - ' || SQLERRM);
END crear_examen;
/



    -- 5) Validar total de preguntas insertadas

SELECT COUNT(*) INTO v_total_insertadas
FROM pregunta_examen
WHERE id_examen = v_new_id;
IF v_total_insertadas != p_numero_preguntas THEN
        RAISE_APPLICATION_ERROR(-20004,
            'Total preguntas asignadas ('||v_total_insertadas||') ≠ '||p_numero_preguntas);
END IF;

    -- 6) Validar que no haya porcentajes NULL

SELECT COUNT(*) INTO v_sin_porcentaje
FROM pregunta_examen
WHERE id_examen = v_new_id
  AND porcentaje_examen IS NULL;
IF v_sin_porcentaje > 0 THEN
        RAISE_APPLICATION_ERROR(-20005,
            'Hay '||v_sin_porcentaje||' preguntas sin porcentaje definido');
END IF;

    p_mensaje := 'Examen '||v_new_id||' creado exitosamente en modo '||p_modo;
EXCEPTION
    WHEN OTHERS THEN
        -- Capturar el error para devolverlo al servicio Java
        p_mensaje := 'Error al crear examen: '||SUBSTR(SQLERRM,1,200);
END crear_examen;
/


-- 1) ACTUALIZAR_EXAMEN con restricción si ya hay presentaciones
CREATE OR REPLACE PROCEDURE actualizar_examen(
    p_id_examen              IN examen.id_examen%TYPE,
    p_tiempo_max             IN examen.tiempo_max%TYPE,
    p_numero_preguntas       IN examen.numero_preguntas%TYPE,
    p_porcentaje_curso       IN examen.porcentaje_curso%TYPE,
    p_nombre                 IN examen.nombre%TYPE,
    p_descripcion            IN examen.descripcion%TYPE,
    p_porcentaje_aprobatorio IN examen.porcentaje_aprobatorio%TYPE,
    p_fecha_inicio           IN examen.fecha_hora_inicio%TYPE,
    p_fecha_fin              IN examen.fecha_hora_fin%TYPE,
    p_estado                 IN examen.estado%TYPE,
    p_mensaje                OUT VARCHAR2
) AS
    v_count_presentaciones NUMBER;
BEGIN
SELECT COUNT(*)
INTO v_count_presentaciones
FROM presentacion_examen
WHERE id_examen = p_id_examen;

IF v_count_presentaciones > 0 THEN
        p_mensaje := 'No se puede modificar el examen: ya tiene presentaciones registradas';
        RETURN;
END IF;

UPDATE examen
SET tiempo_max             = p_tiempo_max,
    numero_preguntas       = p_numero_preguntas,
    porcentaje_curso       = p_porcentaje_curso,
    nombre                 = p_nombre,
    descripcion            = p_descripcion,
    porcentaje_aprobatorio = p_porcentaje_aprobatorio,
    fecha_hora_inicio      = p_fecha_inicio,
    fecha_hora_fin         = p_fecha_fin,
    estado                 = p_estado
WHERE id_examen = p_id_examen;

p_mensaje := 'Examen actualizado exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        p_mensaje := 'Error al actualizar examen: ' || SQLERRM;
END actualizar_examen;
/


-- 2) ASIGNAR_PREGUNTAS en base a dificultad y tu tabla PREGUNTA_EXAMEN
create or replace NONEDITIONABLE PROCEDURE asignar_preguntas(
    p_id_examen   IN NUMBER,
    p_total       IN NUMBER,
    p_pct_facil   IN NUMBER,
    p_pct_media   IN NUMBER,
    p_pct_dificil IN NUMBER
) AS
    n_facil          PLS_INTEGER;
    n_media          PLS_INTEGER;
    n_dificil        PLS_INTEGER;
    v_id_tema_examen pregunta.id_tema%TYPE;
    v_pct_pregunta   NUMBER;
    v_preguntas_calculadas PLS_INTEGER;
    v_preguntas_restantes PLS_INTEGER;
    v_candidate_count PLS_INTEGER; -- Para depuración
BEGIN
    -- 0. Validar p_total
    IF p_total <= 0 THEN
        DBMS_OUTPUT.PUT_LINE('asignar_preguntas: p_total debe ser mayor que 0.');
        RETURN;
END IF;
    v_pct_pregunta := ROUND(100 / p_total, 2);

    -- 1. Obtener el id_tema del examen
BEGIN
SELECT id_tema INTO v_id_tema_examen
FROM examen
WHERE id_examen = p_id_examen;
EXCEPTION
        WHEN NO_DATA_FOUND THEN
            DBMS_OUTPUT.PUT_LINE('asignar_preguntas: No se encontró el examen con ID: ' || p_id_examen);
            RETURN;
END;
    DBMS_OUTPUT.PUT_LINE('asignar_preguntas: Para examen ID ' || p_id_examen || ', tema ID ' || v_id_tema_examen || ', p_total=' || p_total);

    -- 2. Calcula cuántas preguntas de cada nivel
    n_facil   := FLOOR(p_total * p_pct_facil / 100);
    n_media   := FLOOR(p_total * p_pct_media / 100);
    n_dificil := FLOOR(p_total * p_pct_dificil / 100);
    v_preguntas_calculadas := n_facil + n_media + n_dificil;
    v_preguntas_restantes := p_total - v_preguntas_calculadas;

    IF v_preguntas_restantes > 0 AND p_pct_media > 0 THEN
        n_media := n_media + LEAST(v_preguntas_restantes, 1);
        v_preguntas_restantes := v_preguntas_restantes - LEAST(v_preguntas_restantes, 1);
END IF;
    IF v_preguntas_restantes > 0 AND p_pct_facil > 0 THEN
        n_facil := n_facil + LEAST(v_preguntas_restantes, 1);
        v_preguntas_restantes := v_preguntas_restantes - LEAST(v_preguntas_restantes, 1);
END IF;
    IF v_preguntas_restantes > 0 AND p_pct_dificil > 0 THEN
        n_dificil := n_dificil + v_preguntas_restantes;
    ELSIF v_preguntas_restantes > 0 THEN
        IF p_pct_media > 0 THEN n_media := n_media + v_preguntas_restantes;
        ELSIF p_pct_facil > 0 THEN n_facil := n_facil + v_preguntas_restantes;
ELSE n_media := n_media + v_preguntas_restantes;
END IF;
END IF;
    DBMS_OUTPUT.PUT_LINE('asignar_preguntas: Calculado -> n_facil=' || n_facil || ', n_media=' || n_media || ', n_dificil=' || n_dificil);

    -- 3. Insertar preguntas FÁCILES
    IF n_facil > 0 THEN
        DBMS_OUTPUT.PUT_LINE('Intentando insertar ' || n_facil || ' preguntas FÁCILES para tema ' || v_id_tema_examen);
SELECT COUNT(*) INTO v_candidate_count FROM pregunta p WHERE p.id_tema = v_id_tema_examen AND p.dificultad = 'Facil' AND p.estado = 'ACTIVA' AND p.id_pregunta NOT IN (SELECT pe_existente.id_pregunta FROM pregunta_examen pe_existente WHERE pe_existente.id_examen = p_id_examen);
DBMS_OUTPUT.PUT_LINE('Candidatas FÁCILES encontradas: ' || v_candidate_count);

INSERT INTO pregunta_examen(porcentaje_examen, tiempo_pregunta, tiene_tiempo_maximo, id_pregunta, id_examen, origen)
SELECT v_pct_pregunta, NULL, 'N', p_random.id_pregunta, p_id_examen, 'A'
FROM (
         SELECT p.id_pregunta
         FROM pregunta p
         WHERE p.id_tema = v_id_tema_examen AND p.dificultad = 'Facil' AND p.estado = 'ACTIVA' AND p.id_pregunta NOT IN (SELECT pe_existente.id_pregunta FROM pregunta_examen pe_existente WHERE pe_existente.id_examen = p_id_examen)
         ORDER BY DBMS_RANDOM.VALUE
     ) p_random
WHERE ROWNUM <= n_facil;
DBMS_OUTPUT.PUT_LINE('asignar_preguntas: Insertadas ' || SQL%ROWCOUNT || ' preguntas FÁCILES.');
END IF;

    -- 4. Insertar preguntas MEDIAS
    IF n_media > 0 THEN
        DBMS_OUTPUT.PUT_LINE('Intentando insertar ' || n_media || ' preguntas MEDIAS para tema ' || v_id_tema_examen);
SELECT COUNT(*) INTO v_candidate_count FROM pregunta p WHERE p.id_tema = v_id_tema_examen AND p.dificultad = 'Media' AND p.estado = 'ACTIVA' AND p.id_pregunta NOT IN (SELECT pe_existente.id_pregunta FROM pregunta_examen pe_existente WHERE pe_existente.id_examen = p_id_examen);
DBMS_OUTPUT.PUT_LINE('Candidatas MEDIAS encontradas: ' || v_candidate_count);

INSERT INTO pregunta_examen(porcentaje_examen, tiempo_pregunta, tiene_tiempo_maximo, id_pregunta, id_examen, origen)
SELECT v_pct_pregunta, NULL, 'N', p_random.id_pregunta, p_id_examen, 'A'
FROM (
         SELECT p.id_pregunta
         FROM pregunta p
         WHERE p.id_tema = v_id_tema_examen AND p.dificultad = 'Media' AND p.estado = 'ACTIVA' AND p.id_pregunta NOT IN (SELECT pe_existente.id_pregunta FROM pregunta_examen pe_existente WHERE pe_existente.id_examen = p_id_examen)
         ORDER BY DBMS_RANDOM.VALUE
     ) p_random
WHERE ROWNUM <= n_media;
DBMS_OUTPUT.PUT_LINE('asignar_preguntas: Insertadas ' || SQL%ROWCOUNT || ' preguntas MEDIAS.');
END IF;

    -- 5. Insertar preguntas DIFÍCILES
    IF n_dificil > 0 THEN
        DBMS_OUTPUT.PUT_LINE('Intentando insertar ' || n_dificil || ' preguntas DIFÍCILES para tema ' || v_id_tema_examen);
SELECT COUNT(*) INTO v_candidate_count FROM pregunta p WHERE p.id_tema = v_id_tema_examen AND p.dificultad = 'Dificil' AND p.estado = 'ACTIVA' AND p.id_pregunta NOT IN (SELECT pe_existente.id_pregunta FROM pregunta_examen pe_existente WHERE pe_existente.id_examen = p_id_examen);
DBMS_OUTPUT.PUT_LINE('Candidatas DIFÍCILES encontradas: ' || v_candidate_count);

INSERT INTO pregunta_examen(porcentaje_examen, tiempo_pregunta, tiene_tiempo_maximo, id_pregunta, id_examen, origen)
SELECT v_pct_pregunta, NULL, 'N', p_random.id_pregunta, p_id_examen, 'A'
FROM (
         SELECT p.id_pregunta
         FROM pregunta p
         WHERE p.id_tema = v_id_tema_examen AND p.dificultad = 'Dificil' AND p.estado = 'ACTIVA' AND p.id_pregunta NOT IN (SELECT pe_existente.id_pregunta FROM pregunta_examen pe_existente WHERE pe_existente.id_examen = p_id_examen)
         ORDER BY DBMS_RANDOM.VALUE
     ) p_random
WHERE ROWNUM <= n_dificil;
DBMS_OUTPUT.PUT_LINE('asignar_preguntas: Insertadas ' || SQL%ROWCOUNT || ' preguntas DIFÍCILES.');
END IF;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error en asignar_preguntas: ' || SQLCODE || ' - ' || SQLERRM);
        RAISE;
END asignar_preguntas;



-- 2) ACTUALIZAR_PREGUNTA con restricción si ya fue presentada
CREATE OR REPLACE PROCEDURE actualizar_pregunta(
    p_id_pregunta     IN pregunta.id_pregunta%TYPE,
    p_enunciado       IN pregunta.enunciado%TYPE,
    p_es_publica      IN pregunta.es_publica%TYPE,
    p_tipo_pregunta   IN pregunta.tipo_pregunta%TYPE,
    p_estado          IN pregunta.estado%TYPE,
    p_mensaje         OUT VARCHAR2
) AS
    v_count_presentada NUMBER;
BEGIN
SELECT COUNT(*)
INTO v_count_presentada
FROM presentacion_pregunta pp
         JOIN pregunta_examen pe ON pp.id_pregunta = pe.id_pregunta
WHERE pe.id_pregunta = p_id_pregunta;

IF v_count_presentada > 0 THEN
        p_mensaje := 'No se puede modificar la pregunta: ya fue presentada en un examen';
        RETURN;
END IF;


UPDATE pregunta
SET enunciado     = p_enunciado,
    es_publica    = p_es_publica,
    tipo_pregunta = p_tipo_pregunta,
    estado        = p_estado
WHERE id_pregunta = p_id_pregunta;

p_mensaje := 'Pregunta actualizada exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        p_mensaje := 'Error al actualizar pregunta: ' || SQLERRM;
END actualizar_pregunta;
/


-- 5. Inscribir alumno a grupo
CREATE OR REPLACE PROCEDURE inscribir_alumno_grupo(
    p_id_alumno IN alumno.id_alumno%TYPE,
    p_id_grupo  IN grupo.id_grupo%TYPE,
    v_mensaje   OUT VARCHAR2
) AS
BEGIN
    INSERT INTO alumno_grupo(id_alumno, id_grupo)
    VALUES (p_id_alumno, p_id_grupo);
    v_mensaje := 'Alumno inscrito correctamente';
EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        v_mensaje := 'El alumno ya está inscrito en este grupo';
    WHEN OTHERS THEN
        v_mensaje := 'Error al inscribir alumno: ' || SQLERRM;
END inscribir_alumno_grupo;
/

-- 6. Obtener estadísticas de examen
CREATE OR REPLACE PROCEDURE get_estadisticas_examen(
    p_id_examen IN NUMBER,
    res         OUT CLOB
) AS
BEGIN
    SELECT JSON_OBJECT(
               'total_presentaciones' VALUE COUNT(pe.id_presentacion_examen),
               'promedio_nota'        VALUE TO_CHAR(AVG(pe.calificacion), 'FM9990.00'),
               'nota_maxima'          VALUE TO_CHAR(MAX(pe.calificacion)),
               'nota_minima'          VALUE TO_CHAR(MIN(pe.calificacion))
               FORMAT JSON
           )
    INTO res
    FROM presentacion_examen pe
    WHERE pe.id_examen = p_id_examen;
END get_estadisticas_examen;
/



-- 4. Admin: Crear usuario
CREATE OR REPLACE PROCEDURE crear_usuario(
p_id_usuario IN VARCHAR2,
p_rol        IN VARCHAR2,
v_mensaje    OUT VARCHAR2
) AS
BEGIN
INSERT INTO usuario(id_usuario, rol)
VALUES(p_id_usuario, p_rol);
v_mensaje := 'Usuario creado correctamente';
EXCEPTION
WHEN OTHERS THEN
v_mensaje := 'Error al crear usuario: ' || SQLERRM;
END crear_usuario;
/

-- 5. Admin: Actualizar usuario
CREATE OR REPLACE PROCEDURE actualizar_usuario(
p_id_usuario IN VARCHAR2,
p_rol        IN VARCHAR2,
v_mensaje    OUT VARCHAR2
) AS
BEGIN
UPDATE usuario
SET rol = p_rol
WHERE id_usuario = p_id_usuario;
v_mensaje := 'Usuario actualizado correctamente';
EXCEPTION
WHEN OTHERS THEN
v_mensaje := 'Error al actualizar usuario: ' || SQLERRM;
END actualizar_usuario;
/

-- 6. Admin: Eliminar usuario
CREATE OR REPLACE PROCEDURE eliminar_usuario(
p_id_usuario IN VARCHAR2,
v_mensaje    OUT VARCHAR2
) AS
BEGIN
DELETE FROM usuario
WHERE id_usuario = p_id_usuario;
v_mensaje := 'Usuario eliminado correctamente';
EXCEPTION
WHEN OTHERS THEN
v_mensaje := 'Error al eliminar usuario: ' || SQLERRM;
END eliminar_usuario;
/

CREATE OR REPLACE PROCEDURE get_cursos_usuario (
    p_id_usuario IN VARCHAR2,
    rol_in       IN VARCHAR2,
    res          OUT CLOB
) AS
    v_json_result CLOB;
BEGIN
    IF LOWER(rol_in) = 'docente' THEN
SELECT
    COALESCE(
            JSON_ARRAYAGG(
                    JSON_OBJECT(
                            'id_curso'     VALUE id_curso,
                            'nombre_curso' VALUE nombre_curso
                        -- No need for FORMAT JSON on simple string values if Oracle version is recent
                    ) RETURNING CLOB
            ),
            JSON_ARRAY() RETURNING CLOB -- Return empty array if no courses found
    )
INTO v_json_result
FROM (
         SELECT DISTINCT
             c.id_curso   AS id_curso,
             c.nombre     AS nombre_curso
         FROM
             docente d
                 JOIN
             grupo g ON d.id_docente = g.id_docente
                 JOIN
             curso c ON g.id_curso = c.id_curso
         WHERE
             d.id_docente = p_id_usuario
     );
ELSIF LOWER(rol_in) = 'alumno' THEN
SELECT
    COALESCE(
            JSON_ARRAYAGG(
                    JSON_OBJECT(
                            'id_curso'     VALUE id_curso,
                            'nombre_curso' VALUE nombre_curso
                    ) RETURNING CLOB
            ),
            JSON_ARRAY() RETURNING CLOB -- Return empty array if no courses found
    )
INTO v_json_result
FROM (
         SELECT DISTINCT
             c.id_curso   AS id_curso,
             c.nombre     AS nombre_curso
         FROM
             alumno_grupo ag
                 JOIN
             grupo g ON ag.id_grupo = g.id_grupo
                 JOIN
             curso c ON g.id_curso = c.id_curso
         WHERE
             ag.id_alumno = p_id_usuario
     );
ELSE
        -- If rol is not 'docente' or 'alumno', return an empty JSON array
        v_json_result := JSON_ARRAY() RETURNING CLOB;
END IF;

    res := v_json_result;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('Error in get_cursos_usuario: ' || SQLERRM);
        -- Return a valid JSON error object, ensuring it's CLOB
        DECLARE
l_error_msg VARCHAR2(500);
BEGIN
            l_error_msg := 'Error en el procedimiento almacenado: ' || SUBSTR(SQLERRM, 1, 400);
            res := JSON_OBJECT('error' VALUE l_error_msg RETURNING CLOB);
EXCEPTION
          WHEN OTHERS THEN -- Fallback if error JSON creation fails
            res := TO_CLOB('{"error":"Error anidado en el manejador de excepciones."}');
END;
END get_cursos_usuario;
/

CREATE OR REPLACE PROCEDURE get_calificaciones_alumno(
    p_id_alumno IN NUMBER,
    res         OUT CLOB
) AS
BEGIN
    SELECT JSON_ARRAYAGG(
        JSON_OBJECT(
            'examen'       VALUE e.nombre,
            'calificacion' VALUE TO_CHAR(pe.calificacion),
            'fecha'        VALUE TO_CHAR(pe.fecha_hora_presentacion, 'YYYY-MM-DD HH24:MI')
            FORMAT JSON
        )
    )
    INTO res
    FROM presentacion_examen pe
    JOIN examen e ON pe.id_examen = e.id_examen
    WHERE pe.id_alumno = p_id_alumno
      AND pe.calificacion IS NOT NULL;
END get_calificaciones_alumno;
/
--------------------------------------------------------------------------------
-- CRUD de TEMA
--------------------------------------------------------------------------------

-- 1. Crear tema
CREATE OR REPLACE PROCEDURE crear_tema(
    p_id_tema      IN TEMA.ID_TEMA%TYPE,
    p_titulo       IN TEMA.TITULO%TYPE,
    p_descripcion  IN TEMA.DESCRIPCION%TYPE,
    p_unidad       IN TEMA.UNIDAD_ID_UNIDAD%TYPE,
    v_mensaje      OUT VARCHAR2
) AS
BEGIN
    INSERT INTO TEMA(id_tema, titulo, descripcion, unidad_id_unidad)
    VALUES(p_id_tema, p_titulo, p_descripcion, p_unidad);

    v_mensaje := 'Tema creado exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al crear tema: ' || SQLERRM;
END crear_tema;
/
-- 2. Actualizar tema
CREATE OR REPLACE PROCEDURE actualizar_tema(
    p_id_tema      IN TEMA.ID_TEMA%TYPE,
    p_titulo       IN TEMA.TITULO%TYPE,
    p_descripcion  IN TEMA.DESCRIPCION%TYPE,
    p_unidad       IN TEMA.UNIDAD_ID_UNIDAD%TYPE,
    v_mensaje      OUT VARCHAR2
) AS
BEGIN
    UPDATE TEMA
       SET titulo            = p_titulo,
           descripcion       = p_descripcion,
           unidad_id_unidad  = p_unidad
     WHERE id_tema = p_id_tema;

    v_mensaje := 'Tema actualizado exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al actualizar tema: ' || SQLERRM;
END actualizar_tema;
/
-- 3. Eliminar tema
CREATE OR REPLACE PROCEDURE eliminar_tema(
    p_id_tema  IN TEMA.ID_TEMA%TYPE,
    v_mensaje  OUT VARCHAR2
) AS
BEGIN
    DELETE FROM TEMA
     WHERE id_tema = p_id_tema;

    v_mensaje := 'Tema eliminado exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al eliminar tema: ' || SQLERRM;
END eliminar_tema;
/
--------------------------------------------------------------------------------
-- CRUD de GRUPO
--------------------------------------------------------------------------------

-- 1. Crear grupo
CREATE OR REPLACE PROCEDURE crear_grupo(
    p_id_grupo   IN GRUPO.ID_GRUPO%TYPE,
    p_jornada    IN GRUPO.JORNADA%TYPE,
    p_nombre     IN GRUPO.NOMBRE%TYPE,
    p_periodo    IN GRUPO.PERIODO%TYPE,
    p_id_doc     IN GRUPO.ID_DOCENTE%TYPE,
    p_id_curso   IN GRUPO.ID_CURSO%TYPE,
    v_mensaje    OUT VARCHAR2
) AS
BEGIN
    INSERT INTO GRUPO(id_grupo, jornada, nombre, periodo, id_docente, id_curso)
    VALUES(p_id_grupo, p_jornada, p_nombre, p_periodo, p_id_doc, p_id_curso);

    v_mensaje := 'Grupo creado exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al crear grupo: ' || SQLERRM;
END crear_grupo;
/
-- 2. Actualizar grupo
CREATE OR REPLACE PROCEDURE actualizar_grupo(
    p_id_grupo   IN GRUPO.ID_GRUPO%TYPE,
    p_jornada    IN GRUPO.JORNADA%TYPE,
    p_nombre     IN GRUPO.NOMBRE%TYPE,
    p_periodo    IN GRUPO.PERIODO%TYPE,
    p_id_doc     IN GRUPO.ID_DOCENTE%TYPE,
    p_id_curso   IN GRUPO.ID_CURSO%TYPE,
    v_mensaje    OUT VARCHAR2
) AS
BEGIN
    UPDATE GRUPO
       SET jornada    = p_jornada,
           nombre     = p_nombre,
           periodo    = p_periodo,
           id_docente = p_id_doc,
           id_curso   = p_id_curso
     WHERE id_grupo = p_id_grupo;

    v_mensaje := 'Grupo actualizado exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al actualizar grupo: ' || SQLERRM;
END actualizar_grupo;
/
-- 3. Eliminar grupo
CREATE OR REPLACE PROCEDURE eliminar_grupo(
    p_id_grupo IN GRUPO.ID_GRUPO%TYPE,
    v_mensaje  OUT VARCHAR2
) AS
BEGIN
    DELETE FROM GRUPO
     WHERE id_grupo = p_id_grupo;

    v_mensaje := 'Grupo eliminado exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        v_mensaje := 'Error al eliminar grupo: ' || SQLERRM;
END eliminar_grupo;
/

CREATE OR REPLACE PROCEDURE obtener_banco_preguntas(
    v_id_tema     IN  NUMBER,
    p_preguntas   OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_preguntas FOR
        SELECT 
            id_pregunta,
            enunciado,
            es_publica,
            tipo_pregunta,
            id_tema,
            id_docente
        FROM pregunta
        WHERE id_tema = v_id_tema
          AND estado = 'ACTIVA';
END obtener_banco_preguntas;
/

CREATE OR REPLACE PROCEDURE obtener_preguntas_docente(
    v_id_docente   IN  NUMBER,
    p_preguntas    OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_preguntas FOR
        SELECT
            id_pregunta,
            enunciado,
            es_publica,
            tipo_pregunta,
            id_tema,
            id_docente
        FROM pregunta
        WHERE id_docente = v_id_docente
          AND estado     = 'ACTIVA';
END obtener_preguntas_docente;
/

CREATE OR REPLACE PROCEDURE obtener_examenes_docente(
    v_id_docente   IN  NUMBER,
    p_examenes     OUT SYS_REFCURSOR
) AS
BEGIN
    OPEN p_examenes FOR
        SELECT
            id_examen,
            tiempo_max,
            numero_preguntas,
            porcentaje_curso,
            nombre,
            descripcion,
            porcentaje_aprobatorio,
            fecha_hora_inicio,
            fecha_hora_fin,
            num_preguntas_aleatorias,
            id_tema,
            id_docente,
            id_grupo,
            estado
        FROM examen
        WHERE id_docente = v_id_docente
        AND estado     = 'ACTIVO'   -- opcional: solo exámenes activos
        ORDER BY fecha_hora_inicio;
END obtener_examenes_docente;
/

CREATE OR REPLACE PROCEDURE obtener_nota(
    v_id_presentacion_examen IN NUMBER,
    v_nota                   OUT NUMBER
) AS
BEGIN
    -- Intenta leer la calificación de la presentación
    SELECT calificacion
      INTO v_nota
      FROM presentacion_examen
     WHERE id_presentacion_examen = v_id_presentacion_examen;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        -- Si no existe la presentación o no tiene calificación, retornar 0
        v_nota := 0;
    WHEN OTHERS THEN
        -- En caso de cualquier otro error, también devolvemos 0
        v_nota := 0;
END obtener_nota;
/



CREATE OR REPLACE PROCEDURE responder_pregunta(
    p_id_presentacion_examen IN NUMBER,
    p_id_pregunta            IN NUMBER,
    p_id_respuesta           IN NUMBER,
    p_mensaje                OUT VARCHAR2
) AS
    v_terminado CHAR(1);
    v_id_examen NUMBER;
BEGIN
    -- 1. Verificar que la presentación existe y no está finalizada
SELECT terminado, id_examen
INTO v_terminado, v_id_examen
FROM presentacion_examen
WHERE id_presentacion_examen = p_id_presentacion_examen;

IF v_terminado = '1' THEN
        p_mensaje := 'La presentación ya fue finalizada, no puede responder más preguntas';
        RETURN;
END IF;

    -- 2. Verificar que la pregunta pertenece al examen
    DECLARE
v_count NUMBER;
BEGIN
SELECT COUNT(*)
INTO v_count
FROM pregunta_examen pe
WHERE pe.id_examen   = v_id_examen
  AND pe.id_pregunta = p_id_pregunta;
IF v_count = 0 THEN
            p_mensaje := 'La pregunta no pertenece a este examen';
            RETURN;
END IF;
END;

    -- 3. Insertar la respuesta
INSERT INTO presentacion_pregunta (
    id_presentacion_pregunta,
    id_presentacion_examen,
    id_pregunta,
    id_respuesta,
    respuesta_correcta  -- queda NULL hasta calificación
) VALUES (
             presentacion_pregunta_seq.NEXTVAL,
             p_id_presentacion_examen,
             p_id_pregunta,
             p_id_respuesta,
             NULL
         );

p_mensaje := 'Respuesta registrada correctamente';

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        p_mensaje := 'Presentación de examen no encontrada';
WHEN DUP_VAL_ON_INDEX THEN
        p_mensaje := 'Ya existe una respuesta para esta pregunta';
WHEN OTHERS THEN
        p_mensaje := 'Error al registrar respuesta: ' || SQLERRM;
END responder_pregunta;
/



CREATE OR REPLACE PROCEDURE finalizar_presentacion_examen(
    p_id_presentacion_examen IN NUMBER,
    p_mensaje                OUT VARCHAR2
) AS
    v_terminado          CHAR(1);
    v_id_examen          NUMBER;
    v_ex_f_ini           DATE;
    v_ex_f_fin           DATE;
    v_peso_total         NUMBER;
    v_peso_correcto      NUMBER;
    v_nota_final         NUMBER;
BEGIN
    -- 1. Validar existencia y estado
SELECT terminado, id_examen
INTO v_terminado, v_id_examen
FROM presentacion_examen
WHERE id_presentacion_examen = p_id_presentacion_examen;

IF v_terminado = '1' THEN
        p_mensaje := 'La presentación ya fue finalizada';
        RETURN;
END IF;

    -- 2. Validar ventana de tiempo contra la configuración del examen
SELECT fecha_hora_inicio, fecha_hora_fin
INTO v_ex_f_ini, v_ex_f_fin
FROM examen
WHERE id_examen = v_id_examen;

IF SYSDATE < v_ex_f_ini OR SYSDATE > v_ex_f_fin THEN
        p_mensaje := 'Fuera de la ventana de presentación del examen';
        RETURN;
END IF;

    -- 3. Calcular peso total configurado para el examen (debe sumar idealmente 100)
SELECT NVL(SUM(porcentaje_examen), 0)
INTO v_peso_total
FROM pregunta_examen
WHERE id_examen = v_id_examen;

IF v_peso_total = 0 THEN
        p_mensaje := 'No hay preguntas asignadas al examen';
        RETURN;
END IF;

    -- 4. Calcular peso acumulado de respuestas correctas
SELECT NVL(SUM(pe.porcentaje_examen), 0)
INTO v_peso_correcto
FROM presentacion_pregunta pp
         JOIN pregunta_examen pe
              ON pp.id_pregunta = pe.id_pregunta
                  AND pp.id_presentacion_examen = pe.id_examen
WHERE pp.id_presentacion_examen = p_id_presentacion_examen
  AND pp.respuesta_correcta = 'S';

-- 5. Calcular nota final proporcional
v_nota_final := ROUND((v_peso_correcto / v_peso_total) * 100, 2);

    -- 6. Actualizar presentación
UPDATE presentacion_examen
SET terminado    = '1',
    calificacion = v_nota_final
WHERE id_presentacion_examen = p_id_presentacion_examen;

p_mensaje := 'Presentación finalizada. Nota final: ' || v_nota_final;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
        p_mensaje := 'Presentación o examen no encontrada';
WHEN OTHERS THEN
        p_mensaje := 'Error al finalizar presentación: ' || SQLERRM;
END finalizar_presentacion_examen;
/




CREATE OR REPLACE PROCEDURE iniciar_presentacion_examen(
    p_id_examen           IN  NUMBER,
    p_id_alumno           IN  NUMBER,
    p_ip                  IN  VARCHAR2,
    p_id_presentacion     OUT NUMBER,
    p_mensaje             OUT VARCHAR2
) AS
    -- Variables auxiliares
    v_estado       examen.estado%TYPE;
    v_inicio       examen.fecha_hora_inicio%TYPE;
    v_fin          examen.fecha_hora_fin%TYPE;
    v_grupo        examen.id_grupo%TYPE;
    v_pertenece    NUMBER;
    v_intentos     NUMBER;
    -- (Opcional) Límite máximo de intentos por examen
    v_max_intentos CONSTANT NUMBER := 3;
BEGIN
    -- 1) Obtener datos del examen
BEGIN
SELECT estado, fecha_hora_inicio, fecha_hora_fin, id_grupo
INTO v_estado, v_inicio, v_fin, v_grupo
FROM examen
WHERE id_examen = p_id_examen;
EXCEPTION
        WHEN NO_DATA_FOUND THEN
            p_mensaje := 'Examen no encontrado';
            RETURN;
END;

    -- 2) Validar que esté activo y en rango de fechas
    IF v_estado != 'Activa' THEN
        p_mensaje := 'El examen no está activo';
        RETURN;
    ELSIF SYSDATE < v_inicio OR SYSDATE > v_fin THEN
        p_mensaje := 'El examen no está disponible en este horario';
        RETURN;
END IF;

    -- 3) Validar que el alumno pertenezca al grupo
SELECT COUNT(*)
INTO v_pertenece
FROM alumno_grupo
WHERE id_alumno = p_id_alumno
  AND id_grupo  = v_grupo;

IF v_pertenece = 0 THEN
        p_mensaje := 'El alumno no pertenece al grupo de este examen';
        RETURN;
END IF;

    -- 4) (Opcional) Verificar número de intentos
SELECT COUNT(*)
INTO v_intentos
FROM presentacion_examen
WHERE id_examen = p_id_examen
  AND id_alumno = p_id_alumno;

IF v_intentos >= v_max_intentos THEN
        p_mensaje := 'Ha alcanzado el número máximo de intentos (' || v_max_intentos || ')';
        RETURN;
END IF;

    -- 5) Insertar la presentación y devolver el nuevo ID
INSERT INTO presentacion_examen (
    id_presentacion_examen,
    tiempo,
    terminado,
    calificacion,
    ip_source,
    fecha_hora_presentacion,
    id_examen,
    id_alumno
) VALUES (
             presentacion_examen_seq.NEXTVAL,
             NULL,
             'N',
             0,
             p_ip,
             SYSDATE,
             p_id_examen,
             p_id_alumno
         )
    RETURNING id_presentacion_examen INTO p_id_presentacion;

p_mensaje := 'Presentación iniciada (ID=' || p_id_presentacion || ')';
EXCEPTION
    WHEN OTHERS THEN
        p_mensaje := 'Error al iniciar presentación: ' || SQLERRM;
END iniciar_presentacion_examen;
/

--TRIGGERS ====================================================================================================================================
--TRIGGERS


CREATE OR REPLACE TRIGGER trg_agregar_preguntas_examen
AFTER UPDATE OF estado ON examen
    FOR EACH ROW
    WHEN (NEW.estado = 'PUBLICADO')
DECLARE
v_disponibles   INTEGER;
  v_cant_seleccionadas INTEGER;
  v_id_pregunta   NUMBER;
BEGIN

SELECT COUNT(*) INTO v_cant_seleccionadas
FROM pregunta_examen
WHERE id_examen = :NEW.id_examen;

SELECT COUNT(*) INTO v_disponibles
FROM pregunta p
WHERE p.id_tema = :NEW.id_tema
  AND p.es_publica = '1'
  AND p.id_pregunta NOT IN (
    SELECT pe.id_pregunta
    FROM pregunta_examen pe
    WHERE pe.id_examen = :NEW.id_examen
);

IF v_cant_seleccionadas + v_disponibles < :NEW.numero_preguntas THEN
    RAISE_APPLICATION_ERROR(-20002, 'No hay suficientes preguntas para llenar el examen');
END IF;

FOR i IN v_cant_seleccionadas+1 .. :NEW.numero_preguntas LOOP
SELECT id_pregunta
INTO v_id_pregunta
FROM (
         SELECT p.id_pregunta
         FROM pregunta p
         WHERE p.id_tema = :NEW.id_tema
           AND p.es_publica = '1'
           AND p.id_pregunta NOT IN (
             SELECT pe.id_pregunta FROM pregunta_examen pe
             WHERE pe.id_examen = :NEW.id_examen
         )
         ORDER BY DBMS_RANDOM.RANDOM
     ) WHERE ROWNUM = 1;

INSERT INTO pregunta_examen (id_examen, id_pregunta, tiene_tiempo_maximo, origen)
VALUES (:NEW.id_examen, v_id_pregunta, 'N', 'AUTO');
END LOOP;
END;

--Fijar la fecha/hora de presentación automático.
CREATE OR REPLACE TRIGGER trg_establecer_hora_presentacion
BEFORE INSERT ON presentacion_examen
FOR EACH ROW
BEGIN
  IF SYSDATE > (SELECT fecha_hora_fin
                  FROM examen
                 WHERE id_examen = :NEW.id_examen) THEN
    RAISE_APPLICATION_ERROR(-20007,
      'La presentación excede la fecha y hora de fin del examen');
END IF;

  :NEW.FECHA_HORA_PRESENTACION := SYSDATE;
END;




-- TRIGGER QUE SOLO PERMITE LA MODIFICACION DE LA PRESENTACION --EXAMEN SI LA FECHA FINAL DEL EXAMEN ASOCIADO NO ES MENOR A LA --FECHA ACTUAL
CREATE OR REPLACE TRIGGER trg_verificar_fecha_presentacion
BEFORE UPDATE ON presentacion_examen
                  FOR EACH ROW
DECLARE
v_fecha_final DATE;
BEGIN
SELECT fecha_hora_fin INTO v_fecha_final FROM examen WHERE id_examen = :NEW.id_examen;
IF v_fecha_final < SYSDATE THEN
        RAISE_APPLICATION_ERROR(-20001, 'no se puede modificar la presentacion del examen porque la fecha final del examen ya ha pasado');
END IF;
END;

-- este trigger verifica que el tema de la pregunta que se va a adicionar al examen --sea exactamente el mismo que el tema del examen

CREATE OR REPLACE TRIGGER verificar_pregunta_examen
BEFORE INSERT ON pregunta_examen
FOR EACH ROW
DECLARE
v_id_tema_examen pregunta.id_tema%TYPE;
    v_id_tema_pregunta pregunta.id_tema%TYPE;
BEGIN
SELECT id_tema INTO v_id_tema_examen FROM examen WHERE id_examen = :NEW.id_examen;
SELECT id_tema INTO v_id_tema_pregunta FROM pregunta WHERE id_pregunta = :NEW.id_pregunta;
IF v_id_tema_examen != v_id_tema_pregunta THEN
        RAISE_APPLICATION_ERROR(-20003, 'la pregunta no pertenece al tema del examen');
END IF;
END;

-- este trigger verifica que la pregunta que se va a adicionar al examen no pueda --ser una pregunta hija

CREATE OR REPLACE TRIGGER verificar_pregunta_hija
BEFORE INSERT ON pregunta_examen
FOR EACH ROW
DECLARE
v_id_pregunta_compuesta pregunta.id_pregunta_compuesta%TYPE;
BEGIN
SELECT id_pregunta_compuesta INTO v_id_pregunta_compuesta FROM pregunta WHERE id_pregunta = :NEW.id_pregunta;
IF v_id_pregunta_compuesta IS NOT NULL THEN
        RAISE_APPLICATION_ERROR(-20004, 'la pregunta no puede ser hija de otra pregunta');
END IF;
END;

-- este trigger verifica que un examen que ya está en estado publicado no pueda --ser modificado o eliminado si algún estudiante ya ha presentado dicho examen
-- no se toma el estado del examen en cuenta ya que se asume que un examen --que ha sido presentado está en estado publicado
CREATE OR REPLACE TRIGGER verificar_examen_presentado
BEFORE UPDATE OR DELETE ON examen
    FOR EACH ROW
DECLARE
v_presentaciones INTEGER;
BEGIN
SELECT COUNT(*) INTO v_presentaciones FROM presentacion_examen WHERE id_examen = :OLD.id_examen;
IF v_presentaciones > 0 THEN
        RAISE_APPLICATION_ERROR(-20005, 'el examen ya ha sido presentado por un estudiante');
END IF;
END;

-- este trigger verifica que el alumno que va a presentar un examen pertenezca al --grupo al cual se asignó el examen
CREATE OR REPLACE TRIGGER verificar_alumno_grupo
BEFORE INSERT ON presentacion_examen
FOR EACH ROW
DECLARE
v_id_grupo examen.id_grupo%TYPE;
    v_id_grupo_alumno alumno.id_grupo%TYPE;
BEGIN
SELECT id_grupo INTO v_id_grupo FROM examen WHERE id_examen = :NEW.id_examen;
SELECT id_grupo INTO v_id_grupo_alumno FROM alumno WHERE id_alumno = :NEW.id_alumno;
IF v_id_grupo != v_id_grupo_alumno THEN
        RAISE_APPLICATION_ERROR(-20006, 'el alumno no pertenece al grupo al cual se asignó el examen');
END IF;
END;

