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

-- crear un procedimiento que devuelva el nombre del alumno en base a su id
CREATE OR REPLACE PROCEDURE obtener_nombre_alumno(v_id IN VARCHAR2, v_nombre OUT VARCHAR2) AS
    v_nombre_alumno alumno.NOMBRE%TYPE;
BEGIN
    select a.NOMBRE into v_nombre_alumno from alumno a where a.ID_ALUMNO = v_id;
    v_nombre := v_nombre_alumno;
END;

-- crear un procedimiento que devuelva el nombre del docente en base a su id
CREATE OR REPLACE PROCEDURE obtener_nombre_docente(v_id IN VARCHAR2, v_nombre OUT VARCHAR2) AS
    v_nombre_docente docente.NOMBRE%TYPE;
BEGIN
    select d.NOMBRE into v_nombre_docente from docente d where d.ID_DOCENTE = v_id;
    v_nombre := v_nombre_docente;
END;

------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------


-- Procedimiento que retorna los grupos de un usuario dado su id y rol.
-- Retorna un JSON con los grupos del usuario, especficando el id del grupo, el nombre del grupo y el nombre del curso.
create or replace procedure get_grupos_por_usuario(p_id_usuario in varchar2, rol in varchar2, res out clob) as
BEGIN

    if rol = 'docente' then
        -- Retornar los grupos de un docente.
        SELECT JSON_ARRAYAGG(
                       JSON_OBJECT(
                               'id_grupo' VALUE id_grupo,
                               'nombre_grupo' VALUE nombre_grupo,
                               'nombre_curso' VALUE nombre_curso
                               FORMAT JSON
                       )
               )
        INTO res
        from (select g.ID_GRUPO id_grupo,
                     g.NOMBRE   nombre_grupo,
                     c.NOMBRE   nombre_curso
              from docente d
                       join grupo g on (d.ID_DOCENTE = p_id_usuario AND d.id_docente = g.id_docente)
                       join curso c on (c.id_curso = g.id_curso));

    else
        -- Retornar los grupos de un alumno.
        SELECT JSON_ARRAYAGG(
                       JSON_OBJECT(
                               'id_grupo' VALUE id_grupo,
                               'nombre_grupo' VALUE nombre_grupo,
                               'nombre_curso' VALUE nombre_curso
                               FORMAT JSON
                       )
               )
        INTO res
        from (select ag.ID_GRUPO id_grupo, g.NOMBRE nombre_grupo, c.NOMBRE nombre_curso
              from alumno_grupo ag
                       join grupo g on (ag.id_grupo = g.id_grupo)
                       join curso c on (c.id_curso = g.id_curso)
              where ag.id_alumno = p_id_usuario);
    end if;
end;

-----------------------------------------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------------------------------------

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

----------------------------------------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------------------------------------

---

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


-- El siguiente es el procedimiento que consume el backend. Este procedimiento a su vez consume el procedimiento anterior.
-- Este procedimiento devuelve unicamente el nombre del examen que presentó el alumno que viene por parametro, así como la nota de la
-- presentación del examen y el id de la presentación examen.

create or replace procedure get_presentacion_examen_alumno_grupo(p_id_alumno in number, p_id_grupo in number, res out clob) as
    v_examenes SYS_REFCURSOR;
    v_json     CLOB;
BEGIN

    SELECT JSON_ARRAYAGG(
                   JSON_OBJECT(
                           'idExamen' VALUE id_presentacion_examen,
                           'nombreExamen' VALUE nombre_examen,
                           'calificacion' VALUE TO_CHAR(CALIFICACION)
                           FORMAT JSON
                   )
           )
    INTO v_json
    FROM (SELECT pe.id_presentacion_examen id_presentacion_examen,
                 e.nombre                  nombre_examen,
                 pe.CALIFICACION           calificacion
          FROM presentacion_examen pe
                   JOIN examen e ON pe.id_examen = e.id_examen
                   JOIN Grupo g ON e.id_grupo = g.id_grupo
          WHERE pe.CALIFICACION IS NOT NULL
            AND pe.TERMINADO = '1'
            AND pe.id_alumno = p_id_alumno
            AND g.id_grupo = p_id_grupo);
    res := v_json;

END get_presentacion_examen_alumno_grupo;

----------------------------------------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------------------------------------------------------------

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

-- --------------------------------------------------------------------------------------------------------------------------------------------------------
-- ---------------------------------------------------------------------------------------------------------------------------------------------------------
-- ---------------------------------------------------------------------------------------------------------------------------------------------------------
-- ---------------------------------------------------------------------------------------------------------------------------------------------------------


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

--------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------

CREATE OR REPLACE PROCEDURE crear_pregunta (
    v_enunciado       IN pregunta.enunciado%TYPE,
    v_es_publica      IN pregunta.es_publica%TYPE,
    v_tipo_pregunta   IN pregunta.tipo_pregunta%TYPE,
    v_id_tema         IN pregunta.id_tema%TYPE,
    v_id_docente      IN pregunta.id_docente%TYPE,
    v_mensaje         OUT VARCHAR2 -- Mover al final de la lista de parámetros y utilizar OUT
)
IS
BEGIN
    INSERT INTO pregunta (enunciado, es_publica, tipo_pregunta, id_tema, id_docente, estado)
    VALUES (v_enunciado, v_es_publica, v_tipo_pregunta, v_id_tema, v_id_docente, 'creada');
    v_mensaje := 'Pregunta creada exitosamente';

    EXCEPTION
     WHEN OTHERS THEN
            v_mensaje := 'Error al crear la pregunta: ' || SQLERRM;

END crear_pregunta;
/

--------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------------------------------------------------------------------------------------


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