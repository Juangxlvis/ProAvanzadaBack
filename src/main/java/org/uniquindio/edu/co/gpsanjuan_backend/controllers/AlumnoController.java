package org.uniquindio.edu.co.gpsanjuan_backend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.uniquindio.edu.co.gpsanjuan_backend.DTO.*;
import org.uniquindio.edu.co.gpsanjuan_backend.services.interfaces.AlumnoService;

import java.util.List;

@RestController
@RequestMapping("/api/estudiante")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class AlumnoController {
    private final AlumnoService alumnoService;

    @PostMapping("/guardar-pregunta")
    public ResponseEntity<MensajeDTO<String>> guardarPregunta(@RequestBody PreguntaDTO preguntaDTO) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.guardarPregunta(preguntaDTO)));
    }

    // FUNCIONA
    @PostMapping("/obtener-nota")
    public ResponseEntity<MensajeDTO<Float>> obtenerNota(@RequestBody Integer id_presentacion_examen) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.obtenerNotaPresentacionExamen(id_presentacion_examen)));
    }

    @PostMapping("/presentar-examen")
    public ResponseEntity<MensajeDTO<String>> presentarExamen(@RequestBody PresentacionExamenDTO p) {


        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.crearPresentacionExamen(p.tiempo(),
                p.presentado(),p.ipSource(),p.fechaHoraPresentacion(),p.id_examen(),p.id_alumno())));
    }

    @GetMapping("/nombre/{id}/{rol}")
    public ResponseEntity<MensajeDTO<String>> obtenerNombre(@PathVariable String id, @PathVariable String rol) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.obtenerNombre(id, rol)));
    }

    @GetMapping("/cursos/{id}/{rol}")
    public ResponseEntity<MensajeDTO<List<CursoSimpleDTO>>> obtenerCursos(@PathVariable String id, @PathVariable String rol) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.obtenerCursos(id, rol)));
    }


    @GetMapping("/examenes-pendientes/{id}/{id_grupo}")
    public ResponseEntity<MensajeDTO<List<ExamenPendienteDTO>>> obtenerExamenesPendientes(@PathVariable String id, @PathVariable Integer id_grupo) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.obtenerExamenesPendiente(id, id_grupo)));
    }

    @GetMapping("/examenes-hechos/{id}/{id_grupo}")
    public ResponseEntity<MensajeDTO<List<ExamenHechoDTO>>> obtenerExamenesHechos(@PathVariable String id, @PathVariable Integer id_grupo) {
        return ResponseEntity.ok().body(new MensajeDTO<>(false, "", alumnoService.obtenerExamenesHechos(id, id_grupo)));
    }

    @PostMapping("/{idAlumno}/examenes/iniciar") // Ejemplo: POST /api/alumnos/1/examenes/iniciar
    public ResponseEntity<MensajeDTO<ExamenParaPresentarDTO>> iniciarExamen(
            @PathVariable Integer idAlumno, // idAlumno ahora es un PathVariable
            @RequestBody IniciarExamenRequestDTO requestDTO,
            HttpServletRequest httpRequest) {

        String ipCliente = httpRequest.getRemoteAddr();

        if (requestDTO.idExamen() == null) {
            return ResponseEntity.badRequest()
                    .body(new MensajeDTO<>(true, "El idExamen es requerido en el cuerpo de la solicitud.", null));
        }
        if (idAlumno == null) { // Aunque @PathVariable lo haría fallar antes si no se provee
            return ResponseEntity.badRequest()
                    .body(new MensajeDTO<>(true, "El idAlumno es requerido en la ruta.", null));
        }


        try {
            ExamenParaPresentarDTO examenParaPresentar = alumnoService.iniciarCargarExamenParaPresentar(
                    idAlumno,
                    requestDTO.idExamen(),
                    ipCliente
            );
            return ResponseEntity.ok().body(new MensajeDTO<>(false, "Examen listo para presentar.", examenParaPresentar));
        } catch (RuntimeException e) {
            System.err.println("Error en ExamenController al llamar a iniciarYCargarExamen: " + e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeDTO<>(true, e.getMessage(), null));
        } catch (Exception e) {
            System.err.println("Error inesperado en ExamenController: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new MensajeDTO<>(true, "Error interno del servidor al intentar iniciar el examen.", null));
        }
    }


    @PostMapping("/{idPresentacionExamen}/preguntas/responder")
    public ResponseEntity<MensajeDTO<RegistrarRespuestaResponseDTO>> responderPregunta(
            @PathVariable Integer idPresentacionExamen,
            @RequestBody RegistrarRespuestaRequestDTO respuestaDTO
            // ,Principal principal // Para obtener el idAlumno del usuario autenticado
    ) {

        // --- OBTENER ID ALUMNO ---
        // En un sistema real, obtendrías el ID del alumno del contexto de seguridad.
        Integer idAlumno = 1; // !!! EJEMPLO: REEMPLAZA CON TU LÓGICA REAL PARA OBTENER ID_ALUMNO !!!
        // --- FIN OBTENER ID ALUMNO ---

        if (idPresentacionExamen == null || respuestaDTO == null || respuestaDTO.idPregunta() == null) {
            return ResponseEntity.badRequest()
                    .body(new MensajeDTO<>(true, "Faltan datos requeridos (ID Presentación, ID Pregunta).", null));
        }
        // Podrías añadir más validaciones para respuestaDTO.idRespuestaSeleccionada() si es mandatorio

        try {
            RegistrarRespuestaResponseDTO respuestaServicio = alumnoService.registrarRespuestaAlumno(
                    idPresentacionExamen,
                    idAlumno,
                    respuestaDTO
            );
            // El mensaje del servicio ya indica éxito o error específico de PL/SQL
            boolean esErrorLogico = respuestaServicio.mensaje() != null && respuestaServicio.mensaje().toLowerCase().startsWith("error:");

            return ResponseEntity.status(esErrorLogico ? 400 : 200) // Bad Request si es error lógico, OK si no
                    .body(new MensajeDTO<>(esErrorLogico, esErrorLogico ? respuestaServicio.mensaje() : "Respuesta procesada.", respuestaServicio));

        } catch (RuntimeException e) { // Errores lanzados por el servicio
            return ResponseEntity.badRequest().body(new MensajeDTO<>(true, e.getMessage(), null));
        } catch (Exception e) { // Otros errores inesperados
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new MensajeDTO<>(true, "Error interno al registrar la respuesta.", null));
        }
    }
}
