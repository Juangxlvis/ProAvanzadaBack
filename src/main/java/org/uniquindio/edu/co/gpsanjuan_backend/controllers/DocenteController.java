package org.uniquindio.edu.co.gpsanjuan_backend.controllers;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uniquindio.edu.co.gpsanjuan_backend.DTO.*;
import org.uniquindio.edu.co.gpsanjuan_backend.services.interfaces.DocenteService;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/api/docente")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class DocenteController {

    private final DocenteService docenteService;

    @PostMapping("/listarBancoPreguntas")
    public ResponseEntity<MensajeDTO<List<PreguntaBancoDTO>>> listarBancoPreguntas(@RequestBody Integer id_tema) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.obtenerBancoPreguntas(id_tema)));
    }


    @PostMapping("/crearRespuesta")
    public ResponseEntity<MensajeDTO<String>> crearRespuesta(@RequestBody RespuestaDTO respuestaDTO) {
        System.out.println(respuestaDTO.id_pregunta());
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.crearRespuesta(respuestaDTO.descripcion(), respuestaDTO.esVerdadera(), respuestaDTO.id_pregunta())));
    }

    @PostMapping("/crearExamen")
    public ResponseEntity<MensajeDTO<String>> crearExamen(@RequestBody CrearExamenDTO examenDTO) throws ParseException {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.crearExamen(examenDTO)));
    }

    @PostMapping("/crearPregunta")
    public ResponseEntity<MensajeDTO<CrearPreguntaResponseDTO>> crearPregunta(@RequestBody PreguntaDTO datosPregunta) {
        // Imprimir para depuración (opcional)
        System.out.println("Controller - DTO recibido: " + datosPregunta);
        System.out.println("Controller - Enunciado: " + datosPregunta.enunciado());
        System.out.println("Controller - Es Pública: " + datosPregunta.es_publica());
        System.out.println("Controller - Tipo Pregunta: " + datosPregunta.tipo_pregunta()); // Asumiendo que corregiste el nombre en PreguntaDTO
        System.out.println("Controller - ID Tema: " + datosPregunta.id_tema());
        System.out.println("Controller - ID Docente: " + datosPregunta.id_docente());

        try {
            CrearPreguntaResponseDTO respuestaServicio = docenteService.crearPregunta(
                    datosPregunta.enunciado(),
                    datosPregunta.es_publica(),
                    datosPregunta.tipo_pregunta(), // Asumiendo que PreguntaDTO tiene el campo como tipoPregunta
                    datosPregunta.id_tema(),
                    datosPregunta.id_docente()
            );

            // Verificar si el mensaje del servicio indica un error (basado en las validaciones de PL/SQL)
            if (respuestaServicio.mensaje() != null && respuestaServicio.mensaje().toLowerCase().startsWith("error")) {
                // Podrías devolver un HttpStatus.BAD_REQUEST o similar si es un error de validación
                return ResponseEntity.badRequest().body(new MensajeDTO<>(true, respuestaServicio.mensaje(), null));
            }

            return ResponseEntity.ok().body(new MensajeDTO<>(false, "Operación procesada.", respuestaServicio));

        } catch (Exception e) {
            // Capturar excepciones que puedan venir del servicio (ej. error de BD no manejado como OUT param)
            System.err.println("Error en DocenteController al llamar a crearPregunta: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new MensajeDTO<>(true, "Error interno del servidor: " + e.getMessage(), null));
        }
    }

    @PostMapping("/calificarExamen")
    public ResponseEntity<MensajeDTO<String>> calificarExamen(@RequestBody   Integer id_presentacion_examen) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.calificarExamen(id_presentacion_examen)));
    }


    @PostMapping("/obtenerPreguntasDocente")
    public ResponseEntity<MensajeDTO<List<PreguntaBancoDTO>>> obtenerPreguntasDocente (@RequestBody  Integer id_docente) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.obtenerPreguntasDocente(id_docente)));
    }

    @PostMapping("/obtenerExamenesDocente")
    public ResponseEntity<MensajeDTO<List<ExamenDTO>>> obtenerExamenesDocente (@RequestBody  Integer id_docente) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.obtenerExamenesDocente(id_docente)));
    }

    @GetMapping("/nombre/{id}/{rol}")
    public ResponseEntity<MensajeDTO<String>> obtenerNombre(@PathVariable String id, @PathVariable String rol) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.obtenerNombre(id, rol)));
    }
    @GetMapping("/cursos/{id}/{rol}")
    public ResponseEntity<MensajeDTO<List<CursoSimpleDTO>>> obtenerCursos(@PathVariable String id, @PathVariable String rol) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.obtenerCursos(id, rol)));
    }

    @GetMapping("/temasCurso/{id_curso}")
    public ResponseEntity<MensajeDTO<List<TemasCursoDTO>>> obtenerTemasCurso(@PathVariable Integer id_curso) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.obtenerTemasCurso(id_curso)));
    }

    @GetMapping("/{id_docente}/cursos/{id_curso}/grupos")
    public ResponseEntity<MensajeDTO<List<GrupoSimpleDTO>>> obtenerGruposPorCurso(
            @PathVariable("id_docente") Integer idDocente,
            @PathVariable("id_curso") Integer idCurso) {
        try {
            List<GrupoSimpleDTO> grupos = docenteService.obtenerGruposPorCurso(idCurso, idDocente);
            if (grupos.isEmpty()) {
                return ResponseEntity.ok().body(new MensajeDTO<>(false, "No se encontraron grupos para el docente en el curso especificado.", grupos));
            }
            return ResponseEntity.ok().body(new MensajeDTO<>(false, "Grupos obtenidos exitosamente.", grupos));
        } catch (Exception e) {
            // Loggear el error es importante aquí
            System.err.println("Error en DocenteController al llamar a obtenerGruposPorCurso: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new MensajeDTO<>(true, "Error al obtener los grupos del curso: " + e.getMessage(), null));
        }
    }

    @GetMapping("/allTemas")
    public ResponseEntity<MensajeDTO<List<TemasCursoDTO>>> obtenerTemasDocente() {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", docenteService.obtenerTemasDocente()));
    }
}
